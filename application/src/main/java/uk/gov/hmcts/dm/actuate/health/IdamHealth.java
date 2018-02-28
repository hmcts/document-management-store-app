package uk.gov.hmcts.dm.actuate.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty("toggle.includeidamhealth")
public class IdamHealth implements HealthIndicator {

    private final WebChecker idamWebChecker;

    @Autowired
    public IdamHealth(@Value("${auth.idam.client.baseUrl}") String idam) {
        idamWebChecker = new WebChecker("Idam", idam, new RestTemplate());
    }

    @Override
    public Health health() {
        return idamWebChecker.health();
    }
}
