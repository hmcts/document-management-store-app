package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestControllerTest {

    private BlobStorageReadService blobStorageReadService;

    private BlobContainerClient cloudBlobContainer;

    private BlobClient blobClient;

    private BlockBlobClient blob;

    TestController testController;

    @Before
    public void setUp() throws Exception {
        blobStorageReadService = Mockito.mock(BlobStorageReadService.class);
        cloudBlobContainer = Mockito.mock(BlobContainerClient.class);
        blobClient = Mockito.mock(BlobClient.class);
        blob = Mockito.mock(BlockBlobClient.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blob);

        testController = new TestController(blobStorageReadService, cloudBlobContainer);
    }

    @Test
    public void getTrue() {
        when(blobStorageReadService.doesBinaryExist(any())).thenReturn(true);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertTrue(responseEntity.getBody());
    }

    @Test
    public void getFalse() {
        when(blobStorageReadService.doesBinaryExist(any())).thenReturn(false);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertFalse(responseEntity.getBody());
    }

    @Test
    public void uploadCsv() throws Exception {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(file);
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);
        assertTrue(responseEntity.getBody());
    }
}
