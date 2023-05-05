package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SecurityUtilServiceTests {

    @InjectMocks
    SecurityUtilService securityUtilService;

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails serviceDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceDetails);
        when(serviceDetails.getUsername()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    @Test
    public void testSuccessfulRetrievalOfStringFromSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedServiceName());
    }


    @Test
    public void testFailureOfUsernameFromSecurityContextWhenItsNotThere() {
        Assert.assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

}
