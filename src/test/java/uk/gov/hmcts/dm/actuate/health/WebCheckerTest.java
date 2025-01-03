package uk.gov.hmcts.dm.actuate.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.dm.actuate.health.model.HealthCheckResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebCheckerTest {

    private static final String NAME = "test";
    private static final String URL = "http://test.com";
    private static final String HEALTH_URL = URL + "/health";

    private RestTemplate restTemplate = mock(RestTemplate.class);

    @Test
    void healthUp() {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(new HealthCheckResponse("UP"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        assertEquals(Status.UP,webChecker.health().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DOWN", "UNKNOWN"})
    void healthDownVarious(String arg) {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class))
            .thenReturn(new HealthCheckResponse(arg));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

    @Test
    void healthExceptionDown() {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenThrow(new RestClientException("x"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

    @Test
    void healthNoResponseDown() {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(null);
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

    @Test
    void healthBlankResponseDown() {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(new HealthCheckResponse(" "));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        assertEquals(Status.DOWN,webChecker.health().getStatus());
    }
}
