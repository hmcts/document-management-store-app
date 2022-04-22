package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class TestControllerTest {

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blob;

    TestController testController;

    @Before
    public void setUp() throws Exception {

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
        MultipartFile file = Mockito.mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(file);
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);
        assertTrue(responseEntity.getBody());
    }
}
