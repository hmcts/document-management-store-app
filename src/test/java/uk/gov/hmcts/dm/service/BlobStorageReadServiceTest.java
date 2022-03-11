package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.OutputStream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;

    @Mock
    private BlockBlobClient blob;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    private DocumentContentVersion documentContentVersion;
    private OutputStream outputStream;

    @Before
    public void setUp() {
        outputStream = mock(OutputStream.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blob);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() {
        blobStorageReadService.loadBlob(documentContentVersion, outputStream);

        verify(blob).downloadStream(outputStream);
    }

    @Test
    public void doesBinaryExist() {
        given(blob.exists()).willReturn(true);
        Assert.assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
