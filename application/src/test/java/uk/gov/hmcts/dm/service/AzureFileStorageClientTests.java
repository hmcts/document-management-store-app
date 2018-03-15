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

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AzureFileStorageClientTests {

    @Mock
    CloudFileShare cloudFileShare;

    @InjectMocks
    AzureFileStorageClient azureFileStorageClient;

    @Test
    public void testFileReferenceRetrieval() throws Exception {

        CloudFileDirectory rootDirectory = mock(CloudFileDirectory.class);

        when(cloudFileShare.getRootDirectoryReference()).thenReturn(rootDirectory);

        CloudFile cloudFile = mock(CloudFile.class);

        when(rootDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);

        CloudFile returnedCloudFile = azureFileStorageClient.getCloudFile(UUID.randomUUID());

        Assert.assertSame(cloudFile, returnedCloudFile);
    }
}
