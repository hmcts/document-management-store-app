package uk.gov.hmcts.dm.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    @ConditionalOnProperty("idam.s2s-authorised.services")
    public DmServiceAuthFilter dmServiceAuthFilter(ServiceAuthorisationApi authorisationApi,
                                              @Value("${idam.s2s-authorised.services}") List<String> authorisedServices,
                                                   AuthenticationManager authenticationManager) {

        AuthTokenValidator authTokenValidator = new ServiceAuthTokenValidator(authorisationApi);
        DmServiceAuthFilter dmServiceAuthFilter = new DmServiceAuthFilter(authTokenValidator, authorisedServices);
        dmServiceAuthFilter.setAuthenticationManager(authenticationManager);
        return dmServiceAuthFilter;
    }

    @Bean
    @ConditionalOnProperty("idam.s2s-authorised.services")
    public FilterRegistrationBean<DmServiceAuthFilter> registration(DmServiceAuthFilter filter) {
        FilterRegistrationBean<DmServiceAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "preAuthenticatedAuthenticationProvider")
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider =
            new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(
            token -> new User((String) token.getPrincipal(), "N/A", Collections.emptyList())
        );
        return preAuthenticatedAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(preAuthenticatedAuthenticationProvider));
    }

}
