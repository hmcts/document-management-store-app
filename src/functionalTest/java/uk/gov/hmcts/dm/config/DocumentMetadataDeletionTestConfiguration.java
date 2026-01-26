package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.dm.service.DocumentMetadataDeletionService;
import uk.gov.hmcts.dm.service.EmAnnoService;
import uk.gov.hmcts.dm.service.EmNpaService;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.idam.client.IdamClient;

/**
 * Test configuration for DocumentMetadataDeletionService functional tests.
 * This configuration provides the necessary beans for testing the service.
 */
@Configuration
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.dm.client",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.idam.client"
})
@ConditionalOnProperty(name = "toggle.deletemetadatafordocument", havingValue = "true")
public class DocumentMetadataDeletionTestConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") final String secret,
        @Value("${idam.s2s-auth.microservice}") final String microservice,
        final ServiceAuthorisationApi serviceAuthorisationApi) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microservice, serviceAuthorisationApi);
    }

    @Bean
    public DocumentMetadataDeletionService documentMetadataDeletionService(
        EmAnnoService emAnnoService,
        EmNpaService emNpaService,
        AuthTokenGenerator authTokenGenerator,
        IdamClient idamClient,
        @Value("${idam.system-user.username}") String systemUsername,
        @Value("${idam.system-user.password}") String systemPassword) {
        return new DocumentMetadataDeletionService(
            emAnnoService,
            emNpaService,
            authTokenGenerator,
            idamClient,
            systemUsername,
            systemPassword
        );
    }
}
