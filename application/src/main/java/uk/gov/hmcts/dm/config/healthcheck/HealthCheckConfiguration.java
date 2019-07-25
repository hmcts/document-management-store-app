package uk.gov.hmcts.dm.config.healthcheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthIndicatorProperties;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;


//@Configuration
public class HealthCheckConfiguration {

//    @Bean
//    @ConditionalOnProperty("management.health.diskspace.enabled")
    DiskSpaceHealthIndicator diskSpaceHealthIndicator(@Value("${health.disk.threshold}") long threshold) {
//        DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties =
//                new DiskSpaceHealthIndicatorProperties();
//        diskSpaceHealthIndicatorProperties.setThreshold(DataSize.of(threshold, DataUnit.BYTES));
//        return new DiskSpaceHealthIndicator(diskSpaceHealthIndicatorProperties);
        return null;
    }

}
