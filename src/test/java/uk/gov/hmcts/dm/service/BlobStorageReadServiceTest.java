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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    void setUp() {
        lenient().when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        lenient().when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        documentContentVersion = DocumentContentVersion.builder()
            .id(UUID.randomUUID())
            .mimeType("text/plain")
            .originalDocumentName("filename.txt")
            .size(4L)
            .contentUri("someUri")
            .storedDocument(StoredDocument.builder().id(UUID.randomUUID()).build())
            .build();

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
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Content")));
        when(request.getHeaders("Content")).thenReturn(Collections.enumeration(List.of("ContentValue")));

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        verify(blockBlobClient).downloadStream(response.getOutputStream());
    }

    @Test
    void loadsRangedBlobInvalidRangeHeaderStart() {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=A-Z");

        assertThrows(InvalidRangeRequestException.class, () ->
            blobStorageReadService.loadBlob(documentContentVersion, request, response)
        );
    }

    @Test
    void loadsRangedBlobInvalidRangeHeaderStartGreaterThanEnd() {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=1023-0");

        assertThrows(InvalidRangeRequestException.class, () ->
            blobStorageReadService.loadBlob(documentContentVersion, request, response)
        );
    }

    @Test
    void loadsRangedBlobTooLargeRangeHeader() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-1023");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        verify(response, times(0)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        verify(response, times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-4/4");
        verify(response, times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "5");
    }

    @Test
    void loadsRangedBlobInvalidRangeHeader() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-2");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        verify(response, times(1)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        verify(response, times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-2/4");
        verify(response, times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }

    @Test
    void doesBinaryExist() {
        when(blockBlobClient.exists()).thenReturn(true);
        assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

    @Test
    void returnsFullBlobWhenRangeStartIsNegative() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=-2");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        verify(response).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 2-4/4");
        verify(response).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }

    @Test
    void returnsPartialContentWhenRangeHeaderHasValidStartAndEnd() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=1-3");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        verify(response).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        verify(response).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 1-3/4");
        verify(response).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }
}
