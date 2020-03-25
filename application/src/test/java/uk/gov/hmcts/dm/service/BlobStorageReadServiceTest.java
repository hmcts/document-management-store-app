package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class, BlobStorageReadService.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;
    private BlockBlobClient blob;
    private BlobContainerClient cloudBlobContainer;
    private BlobClient blobClient;
    private DocumentContentVersion documentContentVersion;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private OutputStream outputStream;

    @Before
    public void setUp() throws IOException {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blob);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer, request);
    }

    @Test
    public void loadsBlob() throws IOException {
        blobStorageReadService.loadBlob(documentContentVersion, response);
        verify(blob).download(response.getOutputStream());
    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStart() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=A-Z");

        blobStorageReadService.loadBlob(documentContentVersion, response);

    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStartGreaterThanEnd() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=1023-0");

        blobStorageReadService.loadBlob(documentContentVersion, response);

    }

    @Test
    public void loadsRangedBlobTooLargeRangeHeader() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=0-1023");

        blobStorageReadService.loadBlob(documentContentVersion, response);

        // whole doc is returned so we never set the partial content header
        Mockito.verify(response, Mockito.times(0)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-4/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "5");
    }

    @Test
    public void loadsRangedBlobInvalidRangeHeader() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=0-2");

        blobStorageReadService.loadBlob(documentContentVersion, response);

        Mockito.verify(response, Mockito.times(1)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-2/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "3");
    }

    @Test
    public void doesBinaryExist() {
        given(blob.exists()).willReturn(true);
        Assert.assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
