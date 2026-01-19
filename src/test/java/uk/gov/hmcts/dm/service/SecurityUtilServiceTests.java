package uk.gov.hmcts.dm.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilServiceTests {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private SecurityUtilService securityUtilService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testSuccessfulRetrievalOfUsernameFromSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test-user");

        SecurityContextHolder.setContext(securityContext);

        assertEquals("test-user", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void testSuccessfulRetrievalOfStringFromSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("test-service-string");

        SecurityContextHolder.setContext(securityContext);

        assertEquals("test-service-string", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void testFailureOfUsernameFromSecurityContextWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void testFailureOfUsernameFromSecurityContextWhenContextIsEmpty() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    void retrievesUserIdWhenHeaderIsPresent() {
        mockRequestAttributes();
        when(request.getHeader(SecurityUtilService.USER_ID_HEADER)).thenReturn("user123");

        assertEquals("user123", securityUtilService.getUserId());
    }

    @Test
    void returnsNullUserIdWhenRequestIsNull() {
        RequestContextHolder.resetRequestAttributes(); // Ensure null
        assertNull(securityUtilService.getUserId());
    }

    @Test
    void retrievesUserRolesWhenHeaderIsPresent() {
        mockRequestAttributes();
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn("role1, role2, role3");

        Set<String> roles = securityUtilService.getUserRoles();

        assertEquals(Set.of("role1", "role2", "role3"), roles);
    }

    @Test
    void retrievesUserRolesAndTrimsSpaces() {
        mockRequestAttributes();
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn(" role1 , role2 ");

        Set<String> roles = securityUtilService.getUserRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains("role1"));
        assertTrue(roles.contains("role2"));
    }

    @Test
    void returnsNullUserRolesWhenHeaderIsMissing() {
        mockRequestAttributes();
        when(request.getHeader(SecurityUtilService.USER_ROLES_HEADER)).thenReturn(null);

        assertNull(securityUtilService.getUserRoles());
    }

    @Test
    void returnsNullUserRolesWhenRequestIsNull() {
        RequestContextHolder.resetRequestAttributes();
        assertNull(securityUtilService.getUserRoles());
    }

    private void mockRequestAttributes() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
