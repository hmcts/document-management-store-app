package uk.gov.hmcts.dm.actuate.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.dm.actuate.health.model.HealthCheckResponse;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

public class WebChecker {

    private final String name;
    private final String url;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(WebChecker.class);

    @Autowired
    public WebChecker(String name, String url, RestTemplate restTemplate) {
        this.name = name;
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public Health health() {
        final Health.Builder healthBuilder = new Health.Builder();
        return (getStatus()) ? healthBuilder.up().build() : healthBuilder.down().build();
    }


    private boolean getStatus() {
        try {
            final HealthCheckResponse healthCheckResponse = restTemplate.getForObject(url + "/health", HealthCheckResponse.class);
            return "UP".equalsIgnoreCase(healthCheckResponse.getStatus());
        } catch (Exception ex) {
            log.error(name + " " + url + " Failed",keyValue("stackTrace",ex));
            return false;
        }
    }






}
