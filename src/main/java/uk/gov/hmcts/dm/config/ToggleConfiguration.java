package uk.gov.hmcts.dm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by pawel on 16/01/2018.
 */
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
