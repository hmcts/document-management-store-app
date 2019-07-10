package uk.gov.hmcts.dm.config.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

public class ApiV2RequestMatcher implements RequestMatcher {
    @Override
    public boolean matches(HttpServletRequest request) {
        final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        return StringUtils.isNotBlank(acceptHeader) && acceptHeader.matches("(application/vnd.)(.*)(.v2.*\\+json)(.*)");
    }
}
