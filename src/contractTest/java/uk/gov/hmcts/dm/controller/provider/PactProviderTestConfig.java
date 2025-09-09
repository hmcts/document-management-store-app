package uk.gov.hmcts.dm.controller.provider;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.dm.config.V1MediaType;

import java.util.List;

/**
 * This is a TEST-ONLY configuration.
 * It creates a WebMvcConfigurer bean that teaches Spring how to handle the custom
 * HAL media type, fixing the missing Content-Type header issue during Pact tests.
 * This bean is only active when loaded by a test class.
 */
@TestConfiguration
public class PactProviderTestConfig {

    @Bean
    public WebMvcConfigurer pactTestWebMvcConfigurer(ObjectMapper objectMapper) {
        return new WebMvcConfigurer() {
            @Override
            public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
                halConverter.setObjectMapper(objectMapper);
                halConverter.setSupportedMediaTypes(List.of(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE));

                // Add our custom converter to the list used by the application during the test
                converters.add(halConverter);
            }
        };
    }
}
