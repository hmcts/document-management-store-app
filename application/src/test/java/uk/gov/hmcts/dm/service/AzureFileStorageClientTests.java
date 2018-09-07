package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
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
public class AzureFileStorageClientTests {

    @Mock
    CloudFileShare cloudFileShare;

    @InjectMocks
    AzureFileStorageClient azureFileStorageClient;

    @Test
    public void testFileUpload() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        azureFileStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

        verify(cloudFile, times(1)).upload(any(InputStream.class), anyLong());
    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadURISyntaxException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(URISyntaxException.class);

        azureFileStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadStorageException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(StorageException.class);

        azureFileStorageClient.uploadFile(UUID.randomUUID(),
            new MockMultipartFile("x", "x".getBytes("UTF-8")));

    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadIOException() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenThrow(IOException.class);

        azureFileStorageClient.uploadFile(UUID.randomUUID(), file);

    }

    @Test
    public void testFileDelete() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        azureFileStorageClient.deleteFile(UUID.randomUUID());

        verify(cloudFile, times(1)).delete();
    }

    @Test(expected = FileStorageException.class)
    public void testFileDeleteURISyntaxException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(URISyntaxException.class);

        azureFileStorageClient.deleteFile(UUID.randomUUID());

    }

    @Test(expected = FileStorageException.class)
    public void testFileDeleteStorageException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(StorageException.class);

        azureFileStorageClient.deleteFile(UUID.randomUUID());

    }

    @Test
    public void testStreamOfFile() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        azureFileStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

        verify(cloudFile, times(1)).download(any(OutputStream.class));
    }

    @Test(expected = FileStorageException.class)
    public void testStreamOfFileURISyntaxException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(URISyntaxException.class);

        azureFileStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

    }

    @Test(expected = FileStorageException.class)
    public void testStreamOfFileStorageException() throws Exception {

        when(cloudFileShare.getRootDirectoryReference()).thenThrow(StorageException.class);

        azureFileStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

    }

    @Test(expected = StorageException.class)
    public void testInitStorageException() throws Exception {
        when(cloudFileShare.createIfNotExists()).thenThrow(StorageException.class);
        azureFileStorageClient.init();
    }

    @Test
    public void testInit() throws Exception {
        when(cloudFileShare.createIfNotExists()).thenReturn(true);
        azureFileStorageClient.init();
    }
}
