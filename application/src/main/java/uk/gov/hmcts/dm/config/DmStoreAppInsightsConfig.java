package uk.gov.hmcts.dm.config;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;

import uk.gov.hmcts.reform.logging.filters.RequestStatusLoggingFilter;

@Configuration
@ConditionalOnWebApplication
public class DmStoreAppInsightsConfig {

    @Bean
    public WebRequestTrackingFilter webRequestTrackingFilter3(
            @Value("${spring.application.name:application}") String applicationName) {
        return new WebRequestTrackingFilter(applicationName);
    }

    @Bean
    public FilterRegistrationBean requestStatusLoggingFilterRegistrattionBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RequestStatusLoggingFilter());
        filterRegistrationBean.setOrder(HIGHEST_PRECEDENCE + 2);
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean webRequestTrackingFilterRegistrationBean(
            WebRequestTrackingFilter webRequestTrackingFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(webRequestTrackingFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
