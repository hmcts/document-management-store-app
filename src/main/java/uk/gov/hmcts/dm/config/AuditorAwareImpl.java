package uk.gov.hmcts.dm.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
                return Optional.ofNullable(springSecurityUser.getUsername());
            } else if (authentication.getPrincipal() instanceof String) {
                return Optional.ofNullable((String) authentication.getPrincipal());
            }
        }
        return Optional.empty();
    }

}
