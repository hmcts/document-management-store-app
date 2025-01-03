package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    void testEndPointResponseCode() {
        ResponseEntity<Map<String, String>> responseEntity = welcomeController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testEndpointResponseMessage() {
        ResponseEntity<Map<String, String>> responseEntity = welcomeController.welcome();

        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "Welcome to DM Store API!");

        String cacheHeader = responseEntity.getHeaders().getCacheControl();

        assertNotNull(responseEntity);
        assertEquals("no-cache", cacheHeader);
        assertEquals(expectedResponse, responseEntity.getBody());
    }
}
