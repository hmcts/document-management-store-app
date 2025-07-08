package uk.gov.hmcts.dm.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SecurityUtilServiceTests {

    @InjectMocks
    SecurityUtilService securityUtilService;

    @Test
    void testSuccessfulRetrievalOfUsernameFromSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails serviceDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceDetails);
        when(serviceDetails.getUsername()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void testSuccessfulRetrievalOfStringFromSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void testFailureOfUsernameFromSecurityContextWhenItsNotThere() {
        assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void retrievesUserIdWhenHeaderIsPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getHeader(SecurityUtilService.USER_ID_HEADER)).thenReturn("user123");

        assertEquals("user123", securityUtilService.getUserId());
    }

    @Test
    void returnsNullWhenRequestIsNull() {
        RequestContextHolder.setRequestAttributes(null);

        assertNull(securityUtilService.getUserId());
    }

    @Test
    void retrievesUserRolesWhenHeaderIsPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn("role1, role2, role3");

        Set<String> roles = securityUtilService.getUserRoles();

        assertEquals(Set.of("role1", "role2", "role3"), roles);
    }

    @Test
    void returnsNullWhenUserRolesHeaderIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn(null);

        assertNull(securityUtilService.getUserRoles());
    }
}
