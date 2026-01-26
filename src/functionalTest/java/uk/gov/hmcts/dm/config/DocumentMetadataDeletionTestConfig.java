package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.dm.service.DocumentMetadataDeletionService;
import uk.gov.hmcts.dm.service.EmAnnoService;
import uk.gov.hmcts.dm.service.EmNpaService;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;


@Configuration
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.dm.client",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.idam.client"
})
@Import({
    FeignAutoConfiguration.class,
    EmAnnoService.class,
    EmNpaService.class,
    IdamClient.class,
    OAuth2Configuration.class
})
public class DocumentMetadataDeletionTestConfig {

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
