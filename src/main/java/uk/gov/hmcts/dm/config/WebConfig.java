package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pawel on 22/08/2017.
 */
@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WebConfig extends WebMvcConfigurationSupport {

    @Autowired
    Validator validator;

    @Autowired
    ConversionService conversionService;


    @Override
    protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
        ConfigurableWebBindingInitializer initializer = super.getConfigurableWebBindingInitializer();
        initializer.setConversionService(conversionService);
        initializer.setValidator(validator);
        initializer.setPropertyEditorRegistrar(propertyEditorRegistry -> {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            propertyEditorRegistry.registerCustomEditor(Date.class,
                new CustomDateEditor(dateFormatter, true));
        });
        return initializer;
    }
}
