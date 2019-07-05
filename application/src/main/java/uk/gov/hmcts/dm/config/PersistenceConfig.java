package uk.gov.hmcts.dm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
    @Bean
    AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}

class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof ServiceDetails) {
                ServiceDetails userDetails = (ServiceDetails) authentication.getPrincipal();
                return userDetails.getUsername();
            } else if (authentication.getPrincipal() instanceof ServiceAndUserDetails) {
                ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) authentication.getPrincipal();
                return serviceAndUserDetails.getUsername();
            }
        }
        return null;
    }

}
