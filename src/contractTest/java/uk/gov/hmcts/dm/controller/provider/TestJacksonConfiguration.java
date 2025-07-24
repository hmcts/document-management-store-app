package uk.gov.hmcts.dm.controller.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

@TestConfiguration
public class TestJacksonConfiguration {
    @Autowired
    void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new Jackson2HalModule());
    }

    @Bean
    public Jackson2HalModule halModule() {
        return new Jackson2HalModule();
    }
}
