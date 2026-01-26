package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blockBlobClient;

    @InjectMocks
    private TestController testController;

    @Test
    void getTrue() {
        UUID id = UUID.randomUUID();
        when(blobStorageReadService.doesBinaryExist(id)).thenReturn(true);

        ResponseEntity<Boolean> responseEntity = testController.get(id);

        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody());
    }

    @Test
    void getFalse() {
        UUID id = UUID.randomUUID();
        when(blobStorageReadService.doesBinaryExist(id)).thenReturn(false);

        ResponseEntity<Boolean> responseEntity = testController.get(id);

        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody());
    }

    @Test
    void uploadCsv() throws IOException {
        when(cloudBlobContainer.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getName()).thenReturn("test-file-name");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));

        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(Collections.singletonList(file));

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);

        assertEquals(Boolean.TRUE, responseEntity.getBody());

        verify(blockBlobClient).delete();
        verify(blockBlobClient).upload(any(InputStream.class), anyLong());
    }

    @Test
    void uploadCsvShouldContinueWhenDeleteFails() throws IOException {
        when(cloudBlobContainer.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        doThrow(BlobStorageException.class).when(blockBlobClient).delete();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getName()).thenReturn("test-file-name");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));

        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(List.of(file));

        ResponseEntity<Boolean> responseEntity = testController.uploadCsv(command);

        assertEquals(Boolean.TRUE, responseEntity.getBody());

        verify(blockBlobClient).delete();
        verify(blockBlobClient).upload(any(InputStream.class), anyLong());
    }
}
