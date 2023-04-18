package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    public void testEndPointResponseCode() {
        ResponseEntity<Map<String, String>> responseEntity = welcomeController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testEndpointResponseMessage() {
        ResponseEntity<Map<String, String>> responseEntity = welcomeController.welcome();

        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "Welcome to DM Store API!");

        String cacheHeader = responseEntity.getHeaders().getCacheControl();

        assertNotNull(responseEntity);
        assertEquals("no-cache", cacheHeader);
        assertEquals(expectedResponse, responseEntity.getBody());
    }
}
