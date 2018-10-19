package uk.gov.hmcts.dm.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

import java.util.Optional;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private RequestAuthorizer<Service> serviceRequestAuthorizer;

    @Autowired
    private AuthenticationManager authenticationManager;

    private AuthCheckerServiceOnlyFilter serviceOnlyFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        serviceOnlyFilter.setAuthenticationManager(authenticationManager());

        http.headers().cacheControl().disable();

        http
            .addFilter(serviceOnlyFilter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest().authenticated();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers("/swagger-ui.html",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info",
                "/migrate",
                "/");
    }

    @Autowired
    public void setServiceOnlyFilter(Optional<AuthCheckerServiceOnlyFilter> serviceOnlyFilter) {
        this.serviceOnlyFilter = serviceOnlyFilter.orElseGet(() -> {
            AuthCheckerServiceOnlyFilter filter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);
            filter.setAuthenticationManager(authenticationManager);
            return filter;
        });
    }
}
