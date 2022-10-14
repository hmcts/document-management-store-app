package uk.gov.hmcts.dm.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

import java.util.Optional;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {

    @Autowired
    private DmServiceRequestAuthorizer serviceRequestAuthorizer;

    @Autowired
    private AuthenticationManager authenticationManager;

    private AuthCheckerServiceOnlyFilter serviceOnlyFilter;

    @Autowired
    UserDetailsService userDetailsService;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.headers().cacheControl().disable();

        http.requestMatchers()
            .antMatchers("/documents", "/documents/**", "/folders/**")
            .and()
            .addFilter(serviceOnlyFilter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest().authenticated();
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
            web.ignoring().antMatchers(
            "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info",
                "/metrics",
                "/metrics/**",
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
