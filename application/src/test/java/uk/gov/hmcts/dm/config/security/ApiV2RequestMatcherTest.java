package uk.gov.hmcts.dm.config.security;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

public class ApiV2RequestMatcherTest {

    private ApiV2RequestMatcher apiV2RequestMatcher = new ApiV2RequestMatcher();

    @Test
    public void testStringXNotMatching() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("x");
        assertFalse("'X' should not match the API V2 pattern", apiV2RequestMatcher.matches(request));
    }

    @Test
    public void testCorrectPatternMatching() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("application/vnd.document.v2+json");
        assertTrue("'application/vnd.document.v2+json' should not match the API V2 pattern",
            apiV2RequestMatcher.matches(request));
    }

    @Test
    public void testExtendedCorrectPatternMatching() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader(HttpHeaders.ACCEPT))
            .thenReturn("application/vnd.document.v2+json;quality=1;charset=UTF-8");
        assertTrue("'application/vnd.document.v2+json;quality=1;charset=UTF-8' "
                + "should not match the API V2 pattern",
            apiV2RequestMatcher.matches(request));
    }

}
