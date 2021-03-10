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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
public class SecurityUtilService {

    public static final String USER_ID_HEADER = "user-id";
    public static final String USER_ROLES_HEADER = "user-roles";


    public String getUserId() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader(USER_ID_HEADER) : null;
    }

    public Set<String> getUserRoles() {
        HttpServletRequest request = getCurrentRequest();
        return request != null && request.getHeader(USER_ROLES_HEADER) != null
            ? sanitizedSetFrom(Arrays.asList(request.getHeader(USER_ROLES_HEADER).split(",")))
            : null;
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

    public static HashSet<String> sanitizedSetFrom(Collection<String> collection) {
        return collection.stream().map(String::trim).collect(Collectors.toCollection(HashSet::new));
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}
