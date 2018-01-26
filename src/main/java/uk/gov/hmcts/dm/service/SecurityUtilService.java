package uk.gov.hmcts.dm.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import javax.transaction.Transactional;

/**
 * Created by pawel on 23/01/2018.
 */
@Transactional
@Service
public class SecurityUtilService {

    String getCurrentlyAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            ServiceAndUserDetails userDetails = (ServiceAndUserDetails) authentication.getPrincipal();
            if (userDetails != null) {
                return userDetails.getUsername();
            }
        }
        return null;
    }

}
