package uk.gov.hmcts.dm.config.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class AzureStorageConfiguration {

    public static final String AZURE_STORAGE_EMULATOR_AZURITE = "azure-storage-emulator-azurite";
    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container-reference}")
    private String containerReference;

    @Bean
    @ConditionalOnProperty(
        value = "azure.storage.enabled",
        havingValue = "true")
    BlobContainerClient cloudBlobContainer() throws UnknownHostException {
        String blobAddress = connectionString.contains(AZURE_STORAGE_EMULATOR_AZURITE)
            ? connectionString.replace(
            AZURE_STORAGE_EMULATOR_AZURITE,
                InetAddress.getByName(AZURE_STORAGE_EMULATOR_AZURITE).getHostAddress())
            : connectionString;

        final BlobContainerClient client = new BlobContainerClientBuilder()
            .connectionString(blobAddress)
            .containerName(containerReference)
            .buildClient();

        try {
            client.create();
            return client;
        } catch (Exception e) {
            return client;
        }
    }

}
