package uk.gov.hmcts.dm.config.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Configuration
@Profile("azureStorage")
public class AzureStorageConfiguration {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.share-name}")
    private String shareName;

    @Bean
    public CloudStorageAccount storageAccount() throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(connectionString);
    }

    @Bean
    public CloudFileClient cloudFileClient() throws URISyntaxException, InvalidKeyException {
        return storageAccount().createCloudFileClient();
    }

    @Bean
    public CloudFileShare cloudFileShare() throws URISyntaxException, InvalidKeyException, StorageException {
        return cloudFileClient().getShareReference(shareName);
    }

}
