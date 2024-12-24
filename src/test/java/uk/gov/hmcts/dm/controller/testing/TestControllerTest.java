package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class TestControllerTest {

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blob;

    TestController testController;

    @BeforeEach
    public void setUp() {

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        testController = new TestController(blobStorageReadService, cloudBlobContainer);
    }

    @Test
    void getTrue() {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(true);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertTrue(responseEntity.getBody().booleanValue());
    }

    @Test
    void getFalse() {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(false);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertFalse(responseEntity.getBody().booleanValue());
    }

    @Test
    void uploadCsv() throws Exception {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(file);
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);
        assertTrue(responseEntity.getBody());
    }
}
