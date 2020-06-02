package uk.gov.hmcts.dm.config;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import uk.gov.hmcts.dm.errorhandler.ErrorStatusCodeAndMessage;
import uk.gov.hmcts.dm.errorhandler.ExceptionStatusCodeAndMessageResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
@ComponentScan("uk.gov.hmcts.dm.errorhandler")
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableScheduling
public class WebConfig {

    @Value("${connection.upload-timeout-min}")
    private int connectionUploadTimeoutMinutes;

    @Autowired
    private ExceptionStatusCodeAndMessageResolver exceptionStatusCodeAndMessageResolver;

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

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
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {

            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

                List<FieldError> errors = (List<FieldError>)errorAttributes.remove("errors");

                Throwable throwable = getError(webRequest);

                ErrorStatusCodeAndMessage errorStatusCodeAndMessage = exceptionStatusCodeAndMessageResolver
                    .resolveStatusCodeAndMessage(
                        throwable,
                        (String) errorAttributes.get("message"),
                        (Integer) webRequest.getAttribute("javax.servlet.error.status_code", 0),
                        errors);

                errorAttributes.put("error", errorStatusCodeAndMessage.getMessage());
                webRequest.setAttribute("javax.servlet.error.status_code", errorStatusCodeAndMessage.getStatusCode(), 0);
                errorAttributes.put("status", errorStatusCodeAndMessage.getStatusCode());
                if (throwable != null) {
                    log.error(throwable.getMessage(), throwable);
                }

                if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
                    errorAttributes.remove("exception");
                    errorAttributes.remove("trace");
                }

                errorAttributes.remove("message");

                return errorAttributes;
            }
        };
    }
}
