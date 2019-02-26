package uk.gov.hmcts.dm.endtoend;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Configuration
@Profile("local")
public class AzureStorageConfigurationForTesting extends AzureStorageConfiguration {

    @Override
    public Boolean isAzureBlobStoreEnabled() {
        return true;
    }
    
    @Override
    public Boolean isPostgresBlobStorageEnabled() {
        return false;
    }
    
    @Override
    protected boolean shouldCheckExistence() {
        return false;
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        return super.cloudBlobContainer();
    }
}
