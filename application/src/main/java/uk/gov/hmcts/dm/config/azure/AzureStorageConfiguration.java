package uk.gov.hmcts.dm.config.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnProperty(
        value = "azure.storage.enabled",
        havingValue = "true")
    BlobContainerClient cloudBlobContainer() {
        return new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(containerReference)
            .buildClient();
    }

    public Boolean isAzureBlobStoreEnabled() {
        return Optional.ofNullable(azureBlobStorageEnabled).orElse(false);
    }

    public Boolean isPostgresBlobStorageEnabled() {
        return Optional.ofNullable(postgresStorageEnabled).orElse(true);
    }

}
