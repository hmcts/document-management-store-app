package uk.gov.hmcts.dm.controller.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;

@TestConfiguration
public class TestJacksonConfiguration {

    @Bean
    public WebMvcLinkBuilderFactory linkBuilderFactory() {
        return new WebMvcLinkBuilderFactory();
    }


    @Bean
    public Jackson2HalModule halModule() {
        return new Jackson2HalModule();
    }

    @Bean
    public HalConfiguration halConfiguration() {
        return new HalConfiguration();
    }

    @Bean
    public WebMvcLinkBuilderFactory webMvcLinkBuilderFactory() {
        return new WebMvcLinkBuilderFactory();
    }

    @Bean
    public ObjectMapper halObjectMapper(Jackson2HalModule halModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(halModule);
        return objectMapper;
    }
}
