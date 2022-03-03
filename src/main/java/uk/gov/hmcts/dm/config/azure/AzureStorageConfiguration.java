package uk.gov.hmcts.dm.config.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class AzureStorageConfiguration {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container-reference}")
    private String containerReference;

    private static final Logger log = LoggerFactory.getLogger(AzureStorageConfiguration.class);

    @Bean
    @ConditionalOnProperty(
        value = "azure.storage.enabled",
        havingValue = "true")
    BlobContainerClient cloudBlobContainer() throws UnknownHostException {
        String blobAddress = connectionString.contains("azure-storage-emulator-azurite")
            ? connectionString.replace(
                "azure-storage-emulator-azurite",
                InetAddress.getByName("azure-storage-emulator-azurite").getHostAddress())
            : connectionString;

        final BlobContainerClient client = new BlobContainerClientBuilder()
            .connectionString(blobAddress)
            .containerName(containerReference)
            .buildClient();

        try {
            client.create();
            return client;
        } catch (BlobStorageException e) {
            return client;
        }
    }

}
