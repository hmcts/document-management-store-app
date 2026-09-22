package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedJacksonJsonHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class AddMediaTypeSupportConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RequestMappingHandlerAdapter handlerAdapter) {
            configureConverters(handlerAdapter.getMessageConverters());
        }
        return bean;
    }

    private void configureConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
            .filter(converter -> converter instanceof JacksonJsonHttpMessageConverter
                || converter instanceof TypeConstrainedJacksonJsonHttpMessageConverter)
            .forEach(converter -> {
                List<MediaType> vendorSpecificTypes =
                    new ArrayList<>(converter.getSupportedMediaTypes());
                addVendorSpecificMediaTypes(vendorSpecificTypes);
                setSupportedMediaTypes(converter, vendorSpecificTypes);
                registerVendorSpecificMappers(converter, vendorSpecificTypes);
            });
    }

    private void setSupportedMediaTypes(HttpMessageConverter<?> converter, List<MediaType> mediaTypes) {
        if (converter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
            jacksonConverter.setSupportedMediaTypes(mediaTypes);
        }
    }

    private void registerVendorSpecificMappers(HttpMessageConverter<?> converter, List<MediaType> mediaTypes) {
        if (converter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
            Map<MediaType, JsonMapper> registeredMappers =
                jacksonConverter.getMappersForType(RepresentationModel.class);

            if (!registeredMappers.isEmpty()) {
                JsonMapper hypermediaMapper = registeredMappers.values().iterator().next();
                jacksonConverter.registerMappersForType(RepresentationModel.class,
                    mappers -> mediaTypes.forEach(mediaType -> mappers.putIfAbsent(mediaType, hypermediaMapper)));
            }
        }
    }

    private void addVendorSpecificMediaTypes(List<MediaType> mediaTypes) {
        mediaTypes.add(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_AUDIT_ENTRY_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_DOCUMENT_COLLECTION_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_DOCUMENT_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_AUDIT_ENTRY_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE);
        mediaTypes.add(V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE);
    }

}
