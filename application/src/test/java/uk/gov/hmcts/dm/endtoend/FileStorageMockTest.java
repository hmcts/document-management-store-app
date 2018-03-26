package uk.gov.hmcts.dm.endtoend;

import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.service.AzureBlobStorageClient;
import uk.gov.hmcts.dm.service.AzureFileStorageClient;
import uk.gov.hmcts.dm.service.FileStorageClient;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
abstract public class FileStorageMockTest {

    @MockBean
    CloudFileShare cloudFileShare;

    @MockBean
    FileStorageClient fileStorageClient;

    @MockBean
    AzureFileStorageClient azureFileStorageClient;

    @MockBean
    AzureBlobStorageClient azureBlobStorageClient;

    @Before
    public void before() throws Exception {
        CloudFileDirectory cloudFileDirectory = mock(CloudFileDirectory.class);
        CloudFile cloudFile = mock(CloudFile.class);
        when(cloudFileShare.getRootDirectoryReference()).thenReturn(cloudFileDirectory);
        when(cloudFileDirectory.getFileReference(any(String.class))).thenReturn(cloudFile);
    }

}
