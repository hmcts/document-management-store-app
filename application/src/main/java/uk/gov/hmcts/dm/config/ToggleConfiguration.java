package uk.gov.hmcts.dm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "toggle")
@Data
public class ToggleConfiguration {

    private boolean metadatasearchendpoint;
    private boolean documentandmetadatauploadendpoint;
    private boolean folderendpoint;
    private boolean includeidamhealth;
    private boolean ttl;

}
