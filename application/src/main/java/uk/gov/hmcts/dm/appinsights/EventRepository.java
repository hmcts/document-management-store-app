package uk.gov.hmcts.dm.appinsights;

import java.util.Map;

public interface EventRepository {

    void trackEvent(String name, Map<String, String> properties);
}
