package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class BlobStorageReadServiceTest {

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

    private OutputStream outputStream;

    @Before

    public void setUp() throws IOException {
        outputStream = mock(OutputStream.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer, toggleConfiguration);
    }

    @Test
    public void loadsBlob() throws IOException {
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).download(response.getOutputStream());
    }

    @Test
    public void loadsBlobWhileMissingRangeAttribute() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).download(response.getOutputStream());
    }

    @Test
    public void loadsBlobWithHeaderNames() throws IOException {
        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("Content")));
        when(request.getHeaders("Content")).thenReturn(Collections.enumeration(Arrays.asList("ContentValue")));
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blockBlobClient).download(response.getOutputStream());
    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStart() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=A-Z");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStartGreaterThanEnd() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=1023-0");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

    }

    @Test
    public void loadsRangedBlobTooLargeRangeHeader() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-1023");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        // whole doc is returned so we never set the partial content header
        Mockito.verify(response, Mockito.times(0)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-4/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "5");
    }

    @Test
    public void loadsRangedBlobInvalidRangeHeader() throws IOException {

        when(toggleConfiguration.isChunking()).thenReturn(true);
        when(request.getHeader(HttpHeaders.RANGE.toLowerCase())).thenReturn("bytes=0-2");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        Mockito.verify(response, Mockito.times(1)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-2/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }

    @Test
    public void doesBinaryExist() {
        given(blockBlobClient.exists()).willReturn(true);
        Assert.assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
