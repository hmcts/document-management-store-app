package uk.gov.hmcts.dm.actuate.health;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.dm.actuate.health.model.HealthCheckResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebCheckerTest {

    private final static String NAME = "test";
    private final static String URL = "http://test.com";
    private final static String HEALTH_URL = URL + "/health";

    private RestTemplate restTemplate = mock(RestTemplate.class);

    @Test
    public void healthUp() throws Exception {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(new HealthCheckResponse("UP"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        Assert.assertEquals(Status.UP,webChecker.health().getStatus());
    }

    @Test
    public void healthDown() throws Exception {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(new HealthCheckResponse("DOWN"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        Assert.assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

    @Test
    public void healthUknownDown() throws Exception {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenReturn(new HealthCheckResponse("UNKNOWN"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        Assert.assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

    @Test
    public void healthExceptionDown() throws Exception {
        when(restTemplate.getForObject(HEALTH_URL,HealthCheckResponse.class)).thenThrow(new RestClientException("x"));
        WebChecker webChecker = new WebChecker(NAME,URL,restTemplate);
        Assert.assertEquals(Status.DOWN,webChecker.health().getStatus());
    }

}