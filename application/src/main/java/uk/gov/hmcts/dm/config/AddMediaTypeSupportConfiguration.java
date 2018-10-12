package uk.gov.hmcts.dm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AddMediaTypeSupportConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String s) {
        if (bean instanceof RequestMappingHandlerAdapter) {
            ((RequestMappingHandlerAdapter) bean).getMessageConverters().stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .forEach(converter -> {
                    MappingJackson2HttpMessageConverter halConverterCandidate = (MappingJackson2HttpMessageConverter) converter;
                    ObjectMapper objectMapper = halConverterCandidate.getObjectMapper();
                    if (Jackson2HalModule.isAlreadyRegisteredIn(objectMapper)) {
                        List<MediaType> vendorSpecificTypes = new ArrayList<>(halConverterCandidate.getSupportedMediaTypes());
                        vendorSpecificTypes.add(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_HAL_FOLDER_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_HAL_AUDIT_ENTRY_MEDIA_TYPE);

                        vendorSpecificTypes.add(V1MediaType.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE);

                        vendorSpecificTypes.add(V1MediaType.V1_DOCUMENT_COLLECTION_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_DOCUMENT_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_FOLDER_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE);
                        vendorSpecificTypes.add(V1MediaType.V1_AUDIT_ENTRY_MEDIA_TYPE);
                        ((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(vendorSpecificTypes);
                    }
                });
        }
        return bean;

    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) {
        return o;
    }
}
