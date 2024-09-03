package uk.gov.hmcts.dm.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
@EnableGlobalMethodSecurity (prePostEnabled = true)
public class SpringSecurityConfiguration {

    private DmServiceAuthFilter dmServiceAuthFilter;

    private DmServiceServiceFilter dmServiceServiceFilter;

    public SpringSecurityConfiguration(DmServiceAuthFilter dmServiceAuthFilter,
                                       DmServiceServiceFilter dmServiceServiceFilter) {
        this.dmServiceAuthFilter = dmServiceAuthFilter;
        this.dmServiceServiceFilter = dmServiceServiceFilter;
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
    protected SecurityFilterChain deleteFilterChain(HttpSecurity http) throws Exception {

        http.headers(httpSecurityHeadersConfigurer ->
            httpSecurityHeadersConfigurer.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        http.securityMatchers(requestMatcherConfigurer ->
                requestMatcherConfigurer.requestMatchers("/documents/delete/**"))
            .addFilterBefore(dmServiceServiceFilter, AnonymousAuthenticationFilter.class)
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
        return (web) ->
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
