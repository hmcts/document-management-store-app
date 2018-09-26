package uk.gov.hmcts.dm.config.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.dm.exception.AppConfigurationException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Optional;

@Configuration
public class AzureStorageConfiguration {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container-reference}")
    private String containerReference;

    @Value("${azure.storage.enabled}")
    private Boolean azureBlobStorageEnabled;

    @Value("${postgres.storage.enabled}")
    private Boolean postgresStorageEnabled;

    @Bean
    public CloudStorageAccount storageAccount() throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(connectionString);
    }

    @Bean
    public CloudBlobClient cloudBlobClient() throws URISyntaxException, InvalidKeyException {
        return storageAccount().createCloudBlobClient();
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        return cloudBlobClient().getContainerReference(containerReference);
    }

    @Bean
    public Boolean blobEnabled() {
        if (!isAzureBlobStoreEnabled() && !isPostgresBlobStorageEnabled()) {
            throw new AppConfigurationException(
                "At least one of Azure and postgres blob storage needs to be enabled"
            );
        }
        return true;
    }

    public Boolean isAzureBlobStoreEnabled() {
        return Optional.ofNullable(azureBlobStorageEnabled).orElse(false);
    }

    public Boolean isPostgresBlobStorageEnabled() {
        return Optional.ofNullable(postgresStorageEnabled).orElse(true);
    }

}
