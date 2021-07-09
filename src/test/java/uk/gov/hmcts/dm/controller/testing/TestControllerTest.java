package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class, BlobStorageException.class})
@PowerMockIgnore({"javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class TestControllerTest {

    private BlobStorageReadService blobStorageReadService;

    private BlobStorageWriteService blobStorageWriteService;

    private BlobContainerClient cloudBlobContainer;

    private BlobClient blobClient;

    private BlockBlobClient blob;

    TestController testController;

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);
        blobStorageReadService = PowerMockito.mock(BlobStorageReadService.class);

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        testController = new TestController(blobStorageReadService, cloudBlobContainer);
    }

    @Test
    public void getTrue() throws Exception {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(true);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertTrue(responseEntity.getBody().booleanValue());
    }

    @Test
    public void getFalse() throws Exception {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(false);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertFalse(responseEntity.getBody().booleanValue());
    }

    @Test
    public void uploadCsv() throws Exception {
        MultipartFile file = PowerMockito.mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(file);
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);
        assertTrue(responseEntity.getBody());
    }
}
