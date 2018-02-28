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
public class IdamServiceHealth implements HealthIndicator {

    private final WebChecker idamServiceWebChecker;

    @Autowired
    public IdamServiceHealth(@Value("${auth.provider.service.client.baseUrl}") String idamService) {
        idamServiceWebChecker = new WebChecker("Idam Service", idamService, new RestTemplate());
    }

    @Override
    public Health health() {
        return idamServiceWebChecker.health();
    }
}
