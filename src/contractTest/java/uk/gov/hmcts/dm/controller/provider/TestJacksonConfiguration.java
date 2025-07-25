package uk.gov.hmcts.dm.controller.provider;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

@TestConfiguration
public class TestJacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer halModuleCustomizer() {
        return builder -> builder.modulesToInstall(new Jackson2HalModule());
    }
}
