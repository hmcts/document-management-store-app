package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
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

    @Test
    public void testFileDelete() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        azureFileStorageClient.deleteFile(UUID.randomUUID());

        verify(cloudFile, times(1)).delete();
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
}
