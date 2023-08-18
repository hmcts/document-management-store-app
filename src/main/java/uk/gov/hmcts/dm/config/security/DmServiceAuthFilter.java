package uk.gov.hmcts.dm.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class DmServiceAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final Logger LOG = LoggerFactory.getLogger(DmServiceAuthFilter.class);

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
        String token = request.getHeader(SERVICE_AUTHORIZATION);
        if (token == null) {
            throw new InvalidTokenException("ServiceAuthorization Token is missing");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
