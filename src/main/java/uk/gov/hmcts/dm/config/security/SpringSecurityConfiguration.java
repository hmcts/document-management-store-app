package uk.gov.hmcts.dm.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 Created by pawel on 26/06/2017.
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthCheckerServiceOnlyFilter filter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        filter.setAuthenticationManager(authenticationManager());

        http.headers().cacheControl().disable();

        http
            .addFilter(filter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .antMatchers("/swagger-ui.html",
                    "/webjars/springfox-swagger-ui/**",
                    "/swagger-resources/**",
                    "/v2/**",
                    "/health",
                    "/info"
            ).permitAll()
            .anyRequest().authenticated();
    }


}
