package uk.gov.hmcts.dm.security;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsight {

    @Bean
    public TelemetryClient getTelemetryClient() {
        return new TelemetryClient();
    }
}
