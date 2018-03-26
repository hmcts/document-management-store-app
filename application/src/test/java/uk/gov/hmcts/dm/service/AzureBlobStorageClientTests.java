package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.FileStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AzureBlobStorageClientTests {

    @Mock
    CloudBlobContainer cloudBlobContainer;

    @InjectMocks
    AzureBlobStorageClient azureBlobStorageClient;

    @Test
    public void testFileUpload() throws Exception {

        CloudBlockBlob cloudBlockBlob = mock(CloudBlockBlob.class);

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);

        azureBlobStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

        verify(cloudBlockBlob, times(1)).upload(any(InputStream.class), anyLong());
    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadURISyntaxException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(URISyntaxException.class);

        azureBlobStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadStorageException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(StorageException.class);

        azureBlobStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadIOException() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenThrow(IOException.class);

        azureBlobStorageClient.uploadFile(UUID.randomUUID(), file);

    }

    @Test
    public void testFileDelete() throws Exception {

        CloudBlockBlob cloudBlockBlob = mock(CloudBlockBlob.class);

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);

        azureBlobStorageClient.deleteFile(UUID.randomUUID());

        verify(cloudBlockBlob, times(1)).delete();
    }

    @Test(expected = FileStorageException.class)
    public void testFileDeleteURISyntaxException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(URISyntaxException.class);

        azureBlobStorageClient.deleteFile(UUID.randomUUID());

    }

    @Test(expected = FileStorageException.class)
    public void testFileDeleteStorageException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(StorageException.class);

        azureBlobStorageClient.deleteFile(UUID.randomUUID());

    }

    @Test
    public void testStreamOfFile() throws Exception {

        CloudBlockBlob cloudBlockBlob = mock(CloudBlockBlob.class);

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);

        azureBlobStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

        verify(cloudBlockBlob, times(1)).download(any(OutputStream.class));

    }

    @Test(expected = FileStorageException.class)
    public void testStreamOfFileURISyntaxException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(URISyntaxException.class);

        azureBlobStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

    }

    @Test(expected = FileStorageException.class)
    public void testStreamOfFileStorageException() throws Exception {

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenThrow(StorageException.class);

        azureBlobStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

    }

    @Test(expected = StorageException.class)
    public void testInitStorageException() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenThrow(StorageException.class);
        azureBlobStorageClient.init();
    }

    @Test
    public void testInit() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenReturn(true);
        azureBlobStorageClient.init();
    }

}
