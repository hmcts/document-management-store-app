package uk.gov.hmcts.dm.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.util.List;

class DmServiceAuthFilterTest {

    private DmServiceAuthFilter dmServiceAuthFilter;

    private static final String EM_GW = "em_gw";

    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";

    private static final String SSCS = "sscs";

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServiceAuthTokenValidator authTokenValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test()
    @DisplayName("SSCS calling update endpoint")
    void getPreAuthenticatedPrincipalSSCSUpdate() {
        dmServiceAuthFilter = new DmServiceAuthFilter( authTokenValidator,
            List.of(CCD_CASE_DISPOSER, SSCS), List.of(CCD_CASE_DISPOSER));
        Mockito.when(request.getHeader(DmServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn("");
        Mockito.when(request.getRequestURI()).thenReturn("/documents/update");
        Mockito.when(authTokenValidator.getServiceName(Mockito.anyString())).thenReturn(SSCS);
        Object response = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);
        Assertions.assertEquals(SSCS, response);
    }

    @Test()
    @DisplayName("SSCS calling delete endpoint")
    void getPreAuthenticatedPrincipalSSCSDelete() {
        dmServiceAuthFilter = new DmServiceAuthFilter( authTokenValidator,
            List.of(CCD_CASE_DISPOSER, SSCS), List.of(CCD_CASE_DISPOSER));
        Mockito.when(request.getHeader(DmServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn("");
        Mockito.when(request.getRequestURI()).thenReturn("/documents/delete");
        Mockito.when(authTokenValidator.getServiceName(Mockito.anyString())).thenReturn(SSCS);
        Object response = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);
        Assertions.assertNull(response);
    }

    @Test()
    @DisplayName("Ccd CaseDisposer calling delete endpoint")
    void getPreAuthenticatedPrincipalCaseDisposerDelete() {
        dmServiceAuthFilter = new DmServiceAuthFilter( authTokenValidator,
            List.of(CCD_CASE_DISPOSER,EM_GW, SSCS), List.of(CCD_CASE_DISPOSER));
        Mockito.when(request.getHeader(DmServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn("");
        Mockito.when(request.getRequestURI()).thenReturn("/documents/delete");
        Mockito.when(authTokenValidator.getServiceName(Mockito.anyString())).thenReturn(CCD_CASE_DISPOSER);
        Object response = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);
        Assertions.assertEquals(CCD_CASE_DISPOSER, response);
    }

    @Test()
    @DisplayName("Not whitelisted service calling any endpoint")
    void getPreAuthenticatedPrincipalNotListedService() {
        dmServiceAuthFilter = new DmServiceAuthFilter( authTokenValidator,
            List.of(CCD_CASE_DISPOSER, SSCS), List.of(CCD_CASE_DISPOSER));
        Mockito.when(request.getHeader(DmServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn("");
        Mockito.when(authTokenValidator.getServiceName(Mockito.anyString())).thenReturn(EM_GW);
        Object response = dmServiceAuthFilter.getPreAuthenticatedPrincipal(request);
        Assertions.assertNull(response);
    }
}
