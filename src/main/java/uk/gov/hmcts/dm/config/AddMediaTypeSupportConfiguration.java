package uk.gov.hmcts.dm.config;

import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.server.mvc.TypeConstrainedJacksonJsonHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AddMediaTypeSupportConfiguration implements ServerHttpMessageConvertersCustomizer {

    @Override
    public void customize(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConverters(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter
                || converter instanceof TypeConstrainedJacksonJsonHttpMessageConverter) {
                List<MediaType> vendorSpecificTypes =
                    new ArrayList<>(converter.getSupportedMediaTypes());
                addVendorSpecificMediaTypes(vendorSpecificTypes);
                setSupportedMediaTypes(converter, vendorSpecificTypes);
            }
        });
    }

    private void setSupportedMediaTypes(HttpMessageConverter<?> converter, List<MediaType> mediaTypes) {
        if (converter instanceof MappingJackson2HttpMessageConverter jackson2Converter) {
            jackson2Converter.setSupportedMediaTypes(mediaTypes);
        } else if (converter instanceof TypeConstrainedJacksonJsonHttpMessageConverter hateoasConverter) {
            hateoasConverter.setSupportedMediaTypes(mediaTypes);
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
