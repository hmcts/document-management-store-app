package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

}
