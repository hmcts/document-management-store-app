package uk.gov.hmcts.dm.config;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedJacksonJsonHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddMediaTypeSupportConfigurationTest {

    @Test
    void addsVendorMediaTypesOnlyToHalConverter() {
        JacksonJsonHttpMessageConverter genericConverter =
            new JacksonJsonHttpMessageConverter(new JsonMapper());
        List<MediaType> genericMediaTypes = List.copyOf(genericConverter.getSupportedMediaTypes());

        TypeConstrainedJacksonJsonHttpMessageConverter halConverter =
            new TypeConstrainedJacksonJsonHttpMessageConverter(
                RepresentationModel.class,
                List.of(MediaType.APPLICATION_JSON),
                new JsonMapper()
            );
        JsonMapper halMapper = new JsonMapper();
        halConverter.registerMappersForType(RepresentationModel.class,
            mappers -> {
                mappers.put(MediaType.parseMediaType("application/hal+json"), halMapper);
                // A second mapper models converters supporting more than one hypermedia format.
                mappers.put(MediaType.parseMediaType("application/uber+json"), new JsonMapper());
            });
        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        handlerAdapter.setMessageConverters(List.of(genericConverter, halConverter));

        new AddMediaTypeSupportConfiguration().postProcessAfterInitialization(handlerAdapter, "handlerAdapter");

        // Generic JSON responses must retain their original converter selection.
        assertEquals(genericMediaTypes, genericConverter.getSupportedMediaTypes());
        assertTrue(halConverter.getSupportedMediaTypes()
            .contains(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE));
        assertTrue(halConverter.canWrite(RepresentationModel.class,
            V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE));
    }
}
