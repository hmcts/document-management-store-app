package uk.gov.hmcts.dm.endtoend;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;

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
