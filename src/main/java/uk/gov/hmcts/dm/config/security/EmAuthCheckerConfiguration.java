package uk.gov.hmcts.dm.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pawel on 26/06/2017.
 */
@Configuration
public class EmAuthCheckerConfiguration {

//    @Bean
//    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
//        Pattern pattern = Pattern.compile("^/users/([^/]+)/.+$");
//
//        return request -> {
//            Matcher matcher = pattern.matcher(request.getRequestURI());
//            boolean matched = matcher.find();
//            return Optional.ofNullable(matched ? matcher.group(1) : null);
//        };
//    }

//    @Bean
//    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
//        return any -> Collections.emptyList();
//    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return any -> Stream.of("sscs", "divorce", "ccd", "em_gw").collect(Collectors.toList());
    }

//    @Bean
//    public AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter(RequestAuthorizer<User> userRequestAuthorizer,
//                                                                           RequestAuthorizer<Service> serviceRequestAuthorizer,
//                                                                           AuthenticationManager authenticationManager) {
//        AuthCheckerServiceAndUserFilter filter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
//        filter.setAuthenticationManager(authenticationManager);
//        return filter;
//    }

    @Bean
    public AuthCheckerServiceOnlyFilter authCheckerServiceFilter(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                                        AuthenticationManager authenticationManager) {
        AuthCheckerServiceOnlyFilter filter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

}
