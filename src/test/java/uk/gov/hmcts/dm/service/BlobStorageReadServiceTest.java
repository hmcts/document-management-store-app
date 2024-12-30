package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;

    private DocumentContentVersion documentContentVersion;

    @Mock
    private BlockBlobClient blockBlobClient;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ToggleConfiguration toggleConfiguration;

    @BeforeEach
    public void setUp() {
        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer, toggleConfiguration);
    }

    @Test
    void loadsBlob() throws IOException {
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).downloadStream(response.getOutputStream());
    }

    @Test
    void loadsBlobWhileMissingRangeAttribute() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).downloadStream(response.getOutputStream());
    }

    @Test
    void loadsBlobWithHeaderNames() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("Content")));
        when(request.getHeaders("Content")).thenReturn(Collections.enumeration(Arrays.asList("ContentValue")));
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).downloadStream(response.getOutputStream());
    }

    @Test
    void loadsRangedBlobInvalidRangeHeaderStart() {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=A-Z");

        assertThrows(InvalidRangeRequestException.class, () -> {
            blobStorageReadService.loadBlob(documentContentVersion, request, response);
        });
    }

    @Test
    void loadsRangedBlobInvalidRangeHeaderStartGreaterThanEnd() {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=1023-0");

        assertThrows(InvalidRangeRequestException.class, () -> {
            blobStorageReadService.loadBlob(documentContentVersion, request, response);
        });
    }

    @Test
    void loadsRangedBlobTooLargeRangeHeader() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-1023");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        // whole doc is returned so we never set the partial content header
        Mockito.verify(response, Mockito.times(0)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-4/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "5");
    }

    @Test
    void loadsRangedBlobInvalidRangeHeader() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-2");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        Mockito.verify(response, Mockito.times(1)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-2/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }

    @Test
    void doesBinaryExist() {
        given(blockBlobClient.exists()).willReturn(true);
        assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
