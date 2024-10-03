package uk.gov.hmcts.dm.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DmServiceAuthFilterTest {

    private static final String EM_GW = "em_gw";

    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";

    private static final String SSCS = "sscs";

    private final List<String> authorisedServices = List.of(SSCS, CCD_CASE_DISPOSER, EM_GW);

    private final List<String> deleteAuthorisedServices = List.of(CCD_CASE_DISPOSER);

    private final HttpServletRequest request = mock(HttpServletRequest.class);

    private final AuthTokenValidator authTokenValidator = mock(AuthTokenValidator.class);

    private final DmServiceAuthFilter dmServiceAuthFilter
        = new DmServiceAuthFilter(authTokenValidator, authorisedServices, deleteAuthorisedServices);

    @Test
    @DisplayName("SSCS calling update endpoint should be successful")
    void shouldReturnServiceNameWhenAuthorized() {
        when(request.getHeader("ServiceAuthorization")).thenReturn("Bearer validToken");
        when(authTokenValidator.getServiceName("Bearer validToken")).thenReturn(SSCS);
        when(request.getRequestURI()).thenReturn("/documents/update");

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);

        assertEquals(SSCS, principal);
    }

    @Test
    @DisplayName("Should return null when service is not authorized")
    void shouldReturnNullWhenNotAuthorized() {
        when(request.getHeader("ServiceAuthorization")).thenReturn("Bearer validToken");
        when(authTokenValidator.getServiceName("Bearer validToken")).thenReturn("serviceC");
        when(request.getRequestURI()).thenReturn("/documents/get");
        when(request.getMethod()).thenReturn("GET");

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);
        Assertions.assertNull(principal);
        verify(authTokenValidator).getServiceName("Bearer validToken");
    }

    @Test
    @DisplayName("SSCS calling DELETE endpoint should return null")
    void shouldReturnNullWhenNotAuthorizedForDelete() {
        when(request.getHeader("ServiceAuthorization")).thenReturn("Bearer validToken");
        when(authTokenValidator.getServiceName("Bearer validToken")).thenReturn(SSCS);
        when(request.getRequestURI()).thenReturn("/documents/delete");

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);

        Assertions.assertNull(principal);
        verify(authTokenValidator).getServiceName("Bearer validToken");
    }

    @Test
    @DisplayName("Ccd CaseDisposer calling DELETE endpoint")
    void shouldLogServiceAuthorizationForDelete() {
        when(request.getHeader("ServiceAuthorization")).thenReturn("Bearer validToken");
        when(authTokenValidator.getServiceName("Bearer validToken")).thenReturn(CCD_CASE_DISPOSER);
        when(request.getRequestURI()).thenReturn("/documents/delete");

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);

        assertEquals(CCD_CASE_DISPOSER, principal);
    }

    @Test
    @DisplayName("Should handle missing token and return null")
    void shouldHandleMissingToken() {
        when(request.getHeader("ServiceAuthorization")).thenReturn(null);

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);

        Assertions.assertNull(principal);
    }

    @Test
    @DisplayName("Should handle invalid token")
    void shouldHandleInvalidToken() {
        when(request.getHeader("ServiceAuthorization")).thenReturn("Bearer invalidToken");
        when(authTokenValidator.getServiceName("Bearer invalidToken"))
            .thenThrow(new InvalidTokenException("Invalid Token"));

        Object principal = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);

        Assertions.assertNull(principal);
        verify(authTokenValidator).getServiceName("Bearer invalidToken");
    }

    @Test
    @DisplayName("Should return 'N/A' as pre-authenticated credentials")
    void shouldReturnNAForPreAuthenticatedCredentials() {
        Object credentials = dmServiceAuthFilter.getPreAuthenticatedCredentials(request);
        assertEquals("N/A", credentials);
    }
}
