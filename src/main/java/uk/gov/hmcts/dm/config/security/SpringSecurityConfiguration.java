package uk.gov.hmcts.dm.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!contract")
public class SpringSecurityConfiguration {

    private final DmServiceAuthFilter dmServiceAuthFilter;
    private final PermissionEvaluator permissionEvaluator;

    public SpringSecurityConfiguration(
        DmServiceAuthFilter dmServiceAuthFilter,
        PermissionEvaluator permissionEvaluator) {
        this.dmServiceAuthFilter = dmServiceAuthFilter;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.headers(httpSecurityHeadersConfigurer ->
            httpSecurityHeadersConfigurer.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        http.securityMatchers(requestMatcherConfigurer ->
                requestMatcherConfigurer.requestMatchers("/documents", "/documents/**"))
            .addFilterBefore(dmServiceAuthFilter, AnonymousAuthenticationFilter.class)
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web ->
            web.ignoring().requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/**",
                "/favicon.ico",
                "/health",
                "/health/liveness",
                "/health/readiness",
                "/status/health",
                "/mappings",
                "/info",
                "/metrics",
                "/metrics/**",
                "/");
    }
}
