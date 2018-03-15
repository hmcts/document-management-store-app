package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.AzureBlobServiceException;

import java.net.URISyntaxException;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AzureFileStorageServiceTests {

    @Mock
    AzureFileStorageClient azureFileStorageClient;

    @InjectMocks
    AzureFileStorageService azureFileStorageService;

    @Test(expected = AzureBlobServiceException.class)
    public void testFileUploadException() throws Exception {
        when(azureFileStorageClient.getCloudFile(any(UUID.class)))
            .thenThrow(new URISyntaxException("x", "x"));
        azureFileStorageService.uploadFile(
            new StoredDocument(),
            new MockMultipartFile("x", "".getBytes("utf-8")),
            "x"
            );

    }
}
