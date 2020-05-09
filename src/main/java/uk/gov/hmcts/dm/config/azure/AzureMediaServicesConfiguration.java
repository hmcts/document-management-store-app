package uk.gov.hmcts.dm.config.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import com.microsoft.rest.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureMediaServicesConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AzureMediaServicesConfiguration.class);

    @Value("${azure.media-services.client.id}")
    private String clientId;

    @Value("${azure.media-services.tenant.id}")
    private String tenantId;

    @Value("${azure.media-services.client.secret}")
    private String clientSecret;

    @Value("${azure.media-services.subscription.id}")
    private String subscriptionId;

    @Bean
    MediaManager mediaManager() {

        MediaManager manager = null;

        try {
            // Connect to media services
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(clientId, tenantId, clientSecret, AzureEnvironment.AZURE);
            credentials.withDefaultSubscriptionId(subscriptionId);

            // MediaManager is the entry point to Azure Media resource management.
            manager = MediaManager
                .configure()
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .authenticate(credentials, credentials.defaultSubscriptionId());
            log.info(" AzureMediaServicesConfiguration created");
            // Signed in.

        } catch (Exception e) {
                log.error(e.getMessage(), e);
        }

        return manager;
    }

}
