package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilServiceTests {

    @InjectMocks
    SecurityUtilService securityUtilService;

    @Test
    public void testSuccessfulRetrievalOfUsernameFromSecurityContext() {
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
    public void testFailureOfUsernameFromSecurityContextWhenItsNotThere() {
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(null);

        Assert.assertNull(securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

}
