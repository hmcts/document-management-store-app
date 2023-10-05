package uk.gov.hmcts.dm.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppInsightsEventRepository implements EventRepository {

    private final TelemetryClient telemetry;

    @Autowired
    public AppInsightsEventRepository(TelemetryClient telemetry) {
        telemetry.getContext().getComponent().setVersion(getClass().getPackage().getImplementationVersion());
        this.telemetry = telemetry;
    }

    @Override
    public void trackEvent(String name, Map<String, String> properties) {
        telemetry.trackEvent(name, properties,null);
    }

}
