package uk.gov.hmcts.dm.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserPair;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class DmSecurityFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthorizer<Service> serviceRequestAuthorizer;
    private final RequestAuthorizer<User> userRequestAuthorizer;
    private ApiV2RequestMatcher apiV2RequestMatcher;

    public DmSecurityFilter(RequestAuthorizer<Service> serviceRequestAuthorizer,
                            RequestAuthorizer<User> userRequestAuthorizer,
                            ApiV2RequestMatcher apiV2RequestMatcher,
                            AuthenticationManager authenticationManager) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.apiV2RequestMatcher = apiV2RequestMatcher;
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        if (apiV2RequestMatcher.matches(request)) {
            Service service = authorizeService(request);
            if (service == null) {
                return null;
            }

            User user = authorizeUser(request);
            if (user == null) {
                return null;
            }

            return new ServiceAndUserPair(service, user);
        } else {
            return authorizeService(request);
        }

    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (apiV2RequestMatcher.matches(request)) {
            return request.getHeader(UserRequestAuthorizer.AUTHORISATION);
        } else {
            return "N/A";
        }
    }

    private User authorizeUser(HttpServletRequest request) {
        try {
            return userRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful user authentication", e);
            return null;
        }
    }

    private Service authorizeService(HttpServletRequest request) {
        try {
            return serviceRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful service authentication", e);
            return null;
        }
    }


}
