package uk.gov.hmcts.dm.functional.config

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageException
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

import java.security.InvalidKeyException

@Configuration
class AzureStorageConfiguration {

    @Autowired
    Environment environment

    @Bean
    CloudBlobClient cloudBlobClient() throws URISyntaxException, InvalidKeyException {
        return storageAccount().createCloudBlobClient()
    }

    @Bean
    CloudBlobContainer cloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        final CloudBlobContainer container = cloudBlobClient().getContainerReference(
            environment.getRequiredProperty('azure.storage.blob-container-reference'))
        if (!container.exists()) {
            // Current behaviour is that system will hang for a very long time
            throw new RuntimeException('Cloub Blob Container does not exist')
        }
        return container
    }

    private CloudStorageAccount storageAccount() throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(environment.getRequiredProperty('azure.storage.connection-string'))
    }

}
