package uk.gov.hmcts.dm.config.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserPair;

import javax.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DmSecurityFilterTest {

    private DmSecurityFilter dmSecurityFilter;

    @Mock(name = "serviceRequestAuthorizer")
    private RequestAuthorizer<Service> serviceRequestAuthorizer;

    @Mock(name = "userRequestAuthorizer")
    private RequestAuthorizer<User> userRequestAuthorizer;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    ApiV2RequestMatcher apiV2RequestMatcher;

    @Before
    public void before() {
        dmSecurityFilter =
            new DmSecurityFilter(serviceRequestAuthorizer, userRequestAuthorizer, apiV2RequestMatcher,
                null);
    }

    @Test
    public void testNoAuthenticationProvided() {
        Object principal = dmSecurityFilter.getPreAuthenticatedPrincipal(httpServletRequest);
        assertNull("Principal should be null if not authentication provided", principal);
    }

    @Test
    public void testGetPrincipalWithDefaultApi() {
        Service service = new Service("serviceX");
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(false);
        Mockito.when(serviceRequestAuthorizer.authorise(httpServletRequest)).thenReturn(service);
        Object principal = dmSecurityFilter.getPreAuthenticatedPrincipal(httpServletRequest);
        assertEquals("Service should be 'serviceX'", service, principal);
    }

    @Test
    public void testGetPrincipalWithV2Api() {
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(true);

        Service service = new Service("serviceX");
        Mockito.when(serviceRequestAuthorizer.authorise(httpServletRequest)).thenReturn(service);

        User user = new User("userX", Stream.of("role1").collect(Collectors.toSet()));
        Mockito.when(userRequestAuthorizer.authorise(httpServletRequest)).thenReturn(user);

        ServiceAndUserPair principal = (ServiceAndUserPair) dmSecurityFilter.getPreAuthenticatedPrincipal(httpServletRequest);
        assertEquals("Service should be 'serviceX'", service, principal.getService());
        assertEquals("User should be 'userX'", user, principal.getUser());
    }

    @Test
    public void testGetPrincipalWithV2ApiServiceNull() {
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(true);

        Mockito.when(serviceRequestAuthorizer.authorise(httpServletRequest)).thenReturn(null);

        User user = new User("userX", Stream.of("role1").collect(Collectors.toSet()));
        Mockito.when(userRequestAuthorizer.authorise(httpServletRequest)).thenReturn(user);

        assertNull(
            "Should be null", dmSecurityFilter.getPreAuthenticatedPrincipal(httpServletRequest));
    }

    @Test
    public void testGetPrincipalWithV2ApiUserNull() {
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(true);

        Service service = new Service("serviceX");
        Mockito.when(serviceRequestAuthorizer.authorise(httpServletRequest)).thenReturn(service);

        Mockito.when(userRequestAuthorizer.authorise(httpServletRequest)).thenReturn(null);

        assertNull(
            "Should be null", dmSecurityFilter.getPreAuthenticatedPrincipal(httpServletRequest));
    }

    @Test
    public void testCredentialsV2() {
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(true);
        Mockito.when(httpServletRequest.getHeader(UserRequestAuthorizer.AUTHORISATION)).thenReturn("X");

        assertEquals(
            "Should be equal to X", "X", dmSecurityFilter.getPreAuthenticatedCredentials(httpServletRequest));
    }

    @Test
    public void testCredentialsV1() {
        Mockito.when(apiV2RequestMatcher.matches(httpServletRequest)).thenReturn(false);
        Mockito.when(httpServletRequest.getHeader(UserRequestAuthorizer.AUTHORISATION)).thenReturn("X");

        assertEquals(
            "Should be equal to N/A", "N/A", dmSecurityFilter.getPreAuthenticatedCredentials(httpServletRequest));
    }
}
