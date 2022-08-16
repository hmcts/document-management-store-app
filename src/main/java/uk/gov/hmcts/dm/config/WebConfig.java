package uk.gov.hmcts.dm.config;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableScheduling
public class WebConfig {

    @Value("${connection.upload-timeout-min}")
    private int connectionUploadTimeoutMinutes;

    @Bean
    public LocaleResolver localeResolver() {
        FixedLocaleResolver localeResolver = new FixedLocaleResolver();
        localeResolver.setDefaultLocale(Locale.UK);
        return localeResolver;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return (tomcat) -> tomcat.addConnectorCustomizers((connector) -> {
            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                AbstractHttp11Protocol<?> protocolHandler = (AbstractHttp11Protocol<?>) connector
                    .getProtocolHandler();
                protocolHandler.setDisableUploadTimeout(false);
                protocolHandler.setConnectionUploadTimeout(1000 * 60 * connectionUploadTimeoutMinutes);
            }
        });
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
