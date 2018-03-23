package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
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

    @Test
    public void testFileDelete() throws Exception {

        CloudBlockBlob cloudBlockBlob = mock(CloudBlockBlob.class);

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);

        azureBlobStorageClient.deleteFile(UUID.randomUUID());

        verify(cloudBlockBlob, times(1)).delete();
    }


    @Test
    public void testStreamOfFile() throws Exception {

        CloudBlockBlob cloudBlockBlob = mock(CloudBlockBlob.class);

        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);

        azureBlobStorageClient.streamFileContent(UUID.randomUUID(), mock(OutputStream.class));

        verify(cloudBlockBlob, times(1)).download(any(OutputStream.class));
    }
}
