package uk.gov.hmcts.dm.config.healthcheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicator;
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created by pawel on 10/07/2017.
 */
@Configuration
public class HealthCheckConfiguration {

    @Bean
    public DiskSpaceHealthIndicator diskSpaceHealthIndicator(@Value("${health.disk.threshold}") long threshold) {
        DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties =
                new DiskSpaceHealthIndicatorProperties();
        diskSpaceHealthIndicatorProperties.setThreshold(threshold);
        return new DiskSpaceHealthIndicator(diskSpaceHealthIndicatorProperties);
    }

}
