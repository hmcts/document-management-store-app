package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityUtilServiceTests {

    SecurityUtilService securityUtilService = new SecurityUtilService();

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenServiceOnlyAuth() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        ServiceDetails serviceDetails = mock(ServiceDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceDetails);
        when(serviceDetails.getUsername()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContextWhenUserAndServiceAuth() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        ServiceAndUserDetails serviceAndUserDetails = mock(ServiceAndUserDetails.class);

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
