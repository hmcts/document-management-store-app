package uk.gov.hmcts.dm.config;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by pawel on 22/08/2017.
 */
@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WebConfig {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    @Bean
    public CustomDateEditor customDateEditor() {
        return new CustomDateEditor(dateFormat, true);
    }

}
