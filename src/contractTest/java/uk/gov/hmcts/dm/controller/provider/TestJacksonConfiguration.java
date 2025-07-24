package uk.gov.hmcts.dm.controller.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

@TestConfiguration
public class TestJacksonConfiguration {
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jackson2HalModule());
        return objectMapper;
    }
}
