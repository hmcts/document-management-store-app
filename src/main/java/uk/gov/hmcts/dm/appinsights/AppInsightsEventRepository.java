package uk.gov.hmcts.dm.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppInsightsEventRepository implements EventRepository {

    private final TelemetryClient telemetry;

    @Autowired
    public AppInsightsEventRepository(@Value("${azure.application-insights.instrumentation-key}")
                                          String instrumentationKey, TelemetryClient telemetry) {
        TelemetryConfiguration.getActive().setInstrumentationKey(instrumentationKey);
        telemetry.getContext().getComponent().setVersion(getClass().getPackage().getImplementationVersion());
        this.telemetry = telemetry;
    }

    @Override
    public void trackEvent(String name, Map<String, String> properties) {
        telemetry.trackEvent(name, properties,null);
    }

}
