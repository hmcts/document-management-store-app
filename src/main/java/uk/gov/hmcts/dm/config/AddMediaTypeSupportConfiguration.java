package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedJacksonJsonHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lets the HAL message converter also produce and accept the V1 vendor media types.
 *
 * <p>Boot 3.x: the HAL converter's default ObjectMapper had the HAL module registered,
 * so adding the media types was enough.
 *
 * <p>Boot 4.x: HAL may come from either
 *  1. a single-mapper TypeConstrainedJacksonJsonHttpMessageConverter (same as 3.x), or
 *  2. a per-type mapper entry for RepresentationModel on the shared Jackson converter.
 *     A media type with no mapper entry gets no mapper at all, so the HAL mapper must
 *     also be registered for each vendor type.
 */
@Configuration
public class AddMediaTypeSupportConfiguration implements BeanPostProcessor {

    private static final List<MediaType> VENDOR_SPECIFIC_TYPES = List.of(
        V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE,
        V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE,
        V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE,
        V1MediaType.V1_HAL_AUDIT_ENTRY_MEDIA_TYPE,
        V1MediaType.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE,
        V1MediaType.V1_DOCUMENT_COLLECTION_MEDIA_TYPE,
        V1MediaType.V1_DOCUMENT_MEDIA_TYPE,
        V1MediaType.V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE,
        V1MediaType.V1_AUDIT_ENTRY_MEDIA_TYPE,
        V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE,
        V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE
    );

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
            requestMappingHandlerAdapter.getMessageConverters()
                .forEach(this::addVendorSpecificMediaTypeSupport);
        }
        return bean;
    }

    private void addVendorSpecificMediaTypeSupport(HttpMessageConverter<?> converter) {
        if (!(converter instanceof JacksonJsonHttpMessageConverter jacksonConverter)) {
            return;
        }

        // Case 1: Spring HATEOAS single-mapper converter (the Boot 3.x situation).
        // Its default mapper is the hypermedia mapper, so only HAL ones are extended.
        if (converter instanceof TypeConstrainedJacksonJsonHttpMessageConverter) {
            if (supportsHal(jacksonConverter)) {
                addSupportedMediaTypes(jacksonConverter);
            }
            return;
        }

        // Case 2: HAL registered as a per-type mapper on the shared Jackson converter.
        // Media types and mapper entries must be changed together, or canWrite() fails.
        JsonMapper halMapper = findHalMapper(jacksonConverter);
        if (halMapper != null) {
            addSupportedMediaTypes(jacksonConverter);
            jacksonConverter.registerMappersForType(RepresentationModel.class,
                mappers -> VENDOR_SPECIFIC_TYPES.forEach(type -> mappers.putIfAbsent(type, halMapper)));
        }
    }

    private boolean supportsHal(JacksonJsonHttpMessageConverter converter) {
        return converter.getSupportedMediaTypes().stream()
            .anyMatch(MediaTypes.HAL_JSON::isCompatibleWith);
    }

    private JsonMapper findHalMapper(JacksonJsonHttpMessageConverter converter) {
        Map<MediaType, JsonMapper> mappers = converter.getMappersForType(RepresentationModel.class);
        return mappers.entrySet().stream()
            .filter(entry -> MediaTypes.HAL_JSON.isCompatibleWith(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private void addSupportedMediaTypes(JacksonJsonHttpMessageConverter converter) {
        Set<MediaType> mediaTypes = new LinkedHashSet<>(converter.getSupportedMediaTypes());
        mediaTypes.addAll(VENDOR_SPECIFIC_TYPES);
        converter.setSupportedMediaTypes(new ArrayList<>(mediaTypes));
    }
}
