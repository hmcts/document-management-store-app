package uk.gov.hmcts.dm.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class EmAuthCheckerConfiguration {

    @Value("#{'${authorization.s2s-names-whitelist}'.split(',')}")
    private List<String> s2sNamesWhiteList;

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return any -> s2sNamesWhiteList;
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return request -> Optional.empty();
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return request -> Collections.EMPTY_LIST;
    }

    @Bean
    public DmSecurityFilter authCheckerServiceAndUserFilter(final RequestAuthorizer<User> userRequestAuthorizer,
                                                                           final RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                                           final AuthenticationManager authenticationManager) {
        DmSecurityFilter dmSecurityFilter = new DmSecurityFilter(
                serviceRequestAuthorizer,
                userRequestAuthorizer,
                new ApiV2RequestMatcher(),
                authenticationManager);
        dmSecurityFilter.setAuthenticationManager(authenticationManager);
        return dmSecurityFilter;
    }

}

