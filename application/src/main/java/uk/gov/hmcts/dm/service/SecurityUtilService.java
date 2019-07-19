package uk.gov.hmcts.dm.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@Transactional
@Service
public class SecurityUtilService {

    public static final String USER_ID_HEADER = "user-id";
    public static final String USER_ROLES_HEADER = "user-roles";


    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof ServiceDetails) {
                HttpServletRequest request = getCurrentRequest();
                return request != null ? request.getHeader(USER_ID_HEADER) : null;
            } else if (authentication.getPrincipal() instanceof ServiceAndUserDetails) {
                ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) authentication.getPrincipal();
                return serviceAndUserDetails.getUsername();
            }
        }
        return null;
    }

    public String[] getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof ServiceDetails) {
                HttpServletRequest request = getCurrentRequest();
                return request != null && request.getHeader(USER_ROLES_HEADER) != null
                        ? request.getHeader(USER_ROLES_HEADER).trim().split("\\s*,\\s*") : null;
            } else if (authentication.getPrincipal() instanceof ServiceAndUserDetails) {
                ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) authentication.getPrincipal();
                return serviceAndUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(size -> new String[size]);
            }
        }
        return null;
    }

    public String getCurrentlyAuthenticatedServiceName() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof ServiceDetails) {
                ServiceDetails userDetails = (ServiceDetails) authentication.getPrincipal();
                return userDetails.getUsername();
            } else if (authentication.getPrincipal() instanceof ServiceAndUserDetails) {
                ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) authentication.getPrincipal();
                return serviceAndUserDetails.getServicename();
            }
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }


}
