package uk.gov.hmcts.dm;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class StorageTestConfiguration {

    @Bean
    @ConditionalOnProperty("azure.storage.connection-string")
    public BlobServiceClient getStorageClient(
        @Value("${azure.storage.connection-string}") String connection) {
        return new BlobServiceClientBuilder()
                .connectionString(connection)
                .buildClient();
    }
}
