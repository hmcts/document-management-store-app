package uk.gov.hmcts.dm.config.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
public class AppInsights extends AbstractAppInsights {

    @Autowired
    public AppInsights(TelemetryClient client) {
        super(client);
    }
}
