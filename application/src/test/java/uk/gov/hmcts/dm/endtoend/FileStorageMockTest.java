package uk.gov.hmcts.dm.endtoend;

import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.service.AzureFileStorageClient;
import uk.gov.hmcts.dm.service.AzureFileStorageService;

import java.util.UUID;

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
    AzureFileStorageClient azureFileStorageClient;

//    @MockBean
//    AzureFileStorageService azureFileStorageService;

    @Before
    public void before() throws Exception {
        CloudFile cloudFile = Mockito.mock(CloudFile.class);
        Mockito.when(azureFileStorageClient.getCloudFile(Mockito.any(UUID.class))).thenReturn(cloudFile);
    }

}
