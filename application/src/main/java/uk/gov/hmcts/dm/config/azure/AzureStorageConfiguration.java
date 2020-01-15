package uk.gov.hmcts.dm.config.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.dm.exception.AppConfigurationException;

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
    public BlobServiceClient cloudBlobClient() {
        return new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }

    @Bean
    BlobContainerClient cloudBlobContainer() {
        final BlobContainerClient containerClient = cloudBlobClient().createBlobContainer(containerReference);
        if (isAzureBlobStoreEnabled() && !containerClient.exists()) {
            // Current behaviour is that system will hang for a very long time
            throw new AppConfigurationException("Cloub Blob Container does not exist");
        }
        return containerClient;
    }

    @Bean
    Boolean blobEnabled() {
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
