package uk.gov.hmcts.dm.componenttests;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

@Configuration
public class TestConfiguration {

    @Bean
    public AuthCheckerServiceOnlyFilter authCheckerServiceFilter(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                                 AuthenticationManager authenticationManager) {
        AuthCheckerServiceOnlyFilter filter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
        filter.setCheckForPrincipalChanges(true);
        filter.setInvalidateSessionOnPrincipalChange(true);
        return filter;
    }

}
