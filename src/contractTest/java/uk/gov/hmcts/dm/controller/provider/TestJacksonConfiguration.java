package uk.gov.hmcts.dm.controller.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;

@TestConfiguration
public class TestJacksonConfiguration {

    @Autowired
    private ApplicationContext context;

    @Bean
    public HalConfiguration halConfiguration() {
        return new HalConfiguration();
    }

    @Bean
    public LinkRelationProvider defaultLinkRelationProvider() {
        return new DefaultLinkRelationProvider();
    }

    @Bean
    public ObjectMapper halObjectMapper(
        HalConfiguration halConfiguration,
        LinkRelationProvider linkRelationProvider
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jackson2HalModule());
        objectMapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(
            linkRelationProvider,
            CurieProvider.NONE,
            new NoOpMessageResolver(),
            halConfiguration,
            context.getAutowireCapableBeanFactory()
        ));
        return objectMapper;
    }

}




