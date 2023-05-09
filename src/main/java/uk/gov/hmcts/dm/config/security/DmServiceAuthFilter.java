package uk.gov.hmcts.dm.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.websphere.WebSpherePreAuthenticatedWebAuthenticationDetailsSource;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DmServiceAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    public static final String AUTHORISATION = "ServiceAuthorization";

    private static final Logger LOG = LoggerFactory.getLogger(ServiceAuthFilter.class);

    private final List<String> authorisedServices;

    private final AuthTokenValidator authTokenValidator;

    public DmServiceAuthFilter(AuthTokenValidator authTokenValidator, List<String> authorisedServices) {

        this.authTokenValidator = authTokenValidator;
        if (authorisedServices == null || authorisedServices.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one service defined");
        }
        this.authorisedServices = authorisedServices.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        try {

            String bearerToken = extractBearerToken(request);
            String serviceName = authTokenValidator.getServiceName(bearerToken);
            if (!authorisedServices.contains(serviceName)) {
                LOG.debug(
                    "service forbidden {} for endpoint: {} method: {} ",
                    serviceName,
                    request.getRequestURI(),
                    request.getMethod()
                );
                return null;
            } else {
                LOG.debug(
                    "service authorized {} for endpoint: {} method: {}  ",
                    serviceName,
                    request.getRequestURI(),
                    request.getMethod()
                );

                return serviceName;
            }
        } catch (InvalidTokenException | ServiceException exception) {
            LOG.warn("Unsuccessful service authentication", exception);
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    private String extractBearerToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORISATION);
        if (token == null) {
            throw new InvalidTokenException("ServiceAuthorization Token is missing");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
