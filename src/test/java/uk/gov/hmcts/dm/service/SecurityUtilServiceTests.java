package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Created by pawel on 24/01/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilServiceTests {

    @InjectMocks
    SecurityUtilService securityUtilService;

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        ServiceAndUserDetails serviceAndUserDetails = mock(ServiceAndUserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getUsername()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        Assert.assertEquals("x", securityUtilService.getCurrentlyAuthenticatedUsername());
    }

    @Test
    public void testFailureOfUsernameFromSecurityContextWhenItsNotThere() {
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(null);

        Assert.assertNull(securityUtilService.getCurrentlyAuthenticatedUsername());
    }

}
