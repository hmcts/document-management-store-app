package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilServiceTests {

    SecurityUtilService securityUtilService = new SecurityUtilService();

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    ServiceDetails serviceDetails;

    @Mock
    ServiceAndUserDetails serviceAndUserDetails;

    @Mock
    HttpServletRequest request;

    @Before
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenServiceOnlyAuth() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceDetails);
        when(serviceDetails.getUsername()).thenReturn("x");
        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }


    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenServiceOnlyAuthWithUserHeaders() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(request.getHeader(SecurityUtilService.USER_ID_HEADER)).thenReturn("u");
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn("R1, R2");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceDetails);
        when(serviceDetails.getUsername()).thenReturn("x");

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
        Assert.assertEquals("u", securityUtilService.getUserId());
        Assert.assertEquals("R1", securityUtilService.getUserRoles()[0]);
        Assert.assertEquals("R2", securityUtilService.getUserRoles()[1]);
    }

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenUserAndServiceAuth() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getServicename()).thenReturn("x");
        when(serviceAndUserDetails.getUsername()).thenReturn("u");
        when(serviceAndUserDetails.getAuthorities())
                .thenReturn(Stream.of(new SimpleGrantedAuthority("R")).collect(Collectors.toList()));

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
        Assert.assertEquals("u", securityUtilService.getUserId());
        Assert.assertEquals("R", securityUtilService.getUserRoles()[0]);
    }


    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenUserAndServiceAuthWithUserHeaders() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(request.getHeader(SecurityUtilService.USER_ID_HEADER)).thenReturn("XXX");
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn("YYY");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getServicename()).thenReturn("x");
        when(serviceAndUserDetails.getUsername()).thenReturn("u");
        when(serviceAndUserDetails.getAuthorities())
                .thenReturn(Stream.of(new SimpleGrantedAuthority("R")).collect(Collectors.toList()));
        SecurityContextHolder.setContext(securityContext);

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
        Assert.assertEquals("u", securityUtilService.getUserId());
        Assert.assertEquals("R", securityUtilService.getUserRoles()[0]);
    }

    @Test
    public void testFailureOfUsernameFromSecurityContextWhenItsNotThere() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(null);

        Assert.assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
        Assert.assertNull(securityUtilService.getUserId());
        Assert.assertNull(securityUtilService.getUserRoles());
    }

}
