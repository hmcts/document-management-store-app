package uk.gov.hmcts.dm.config.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
@ConditionalOnProperty("azure.app_insights_key")
public class AppInsights extends AbstractAppInsights {

    private static final Logger LOG = LoggerFactory.getLogger(AppInsights.class);

    @Autowired
    public AppInsights(TelemetryClient client) {
        super(client);
        LOG.info("Building AppInsights");
    }

    public void trackException(Exception e) {
        telemetry.trackException(e);
    }
}
