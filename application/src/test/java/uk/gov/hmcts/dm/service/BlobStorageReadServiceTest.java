package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.OutputStream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;
    private BlockBlobClient blob;
    private BlobContainerClient cloudBlobContainer;
    private BlobClient blobClient;
    private DocumentContentVersion documentContentVersion;
    private OutputStream outputStream;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        outputStream = mock(OutputStream.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blob);

        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() {
        blobStorageReadService.loadBlob(documentContentVersion, outputStream);

        verify(blob).download(outputStream);
    }

    @Test
    public void doesBinaryExist() {
        given(blob.exists()).willReturn(true);
        Assert.assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
