package uk.gov.hmcts.dm.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

/**
 * Created by pawel on 23/01/2018.
 */
@Transactional
@Service
public class SecurityUtilService {

    public String getUserId() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("user-id") : null;
    }

    public String[] getUserRoles() {
        HttpServletRequest request = getCurrentRequest();
        return request != null && request.getHeader("user-roles") != null
            ? request.getHeader("user-roles").split(",") : null;
    }

    public String getCurrentlyAuthenticatedServiceName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            ServiceDetails userDetails = (ServiceDetails) authentication.getPrincipal();
            if (userDetails != null) {
                return userDetails.getUsername();
            }
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            return servletRequest;
        }
        return null;
    }


}
