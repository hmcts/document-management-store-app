package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
@ExtendWith(MockitoExtension.class)
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;
    private DocumentContentVersion documentContentVersion;

    @Mock
    private BlockBlobClient blob;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() throws IOException {

        blobStorageReadService = Mockito.mock(BlobStorageReadService.class);
        cloudBlobContainer = Mockito.mock(BlobContainerClient.class);
        blobClient = Mockito.mock(BlobClient.class);
        blob = Mockito.mock(BlockBlobClient.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blob);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() throws IOException {
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        verify(blob).download(response.getOutputStream());
    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStart() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=A-Z");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

    }

    @Test(expected = InvalidRangeRequestException.class)
    public void loadsRangedBlobInvalidRangeHeaderStartGreaterThanEnd() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=1023-0");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

    }

    @Test
    public void loadsRangedBlobTooLargeRangeHeader() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=0-1023");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

        // whole doc is returned so we never set the partial content header
        Mockito.verify(response, Mockito.times(0)).setStatus(HttpStatus.PARTIAL_CONTENT.value());
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-4/4");
        Mockito.verify(response, Mockito.times(1)).setHeader(HttpHeaders.CONTENT_LENGTH, "5");
    }

    @Test
    public void loadsRangedBlobInvalidRangeHeader() throws IOException {

        when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=0-2");

        blobStorageReadService.loadBlob(documentContentVersion, request, response);

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
