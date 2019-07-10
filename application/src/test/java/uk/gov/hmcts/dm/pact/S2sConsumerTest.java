package uk.gov.hmcts.dm.pact;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class S2sConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("s2s", "localhost", 4502, this);

    @Pact(provider = "s2s", consumer = "dm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .given("provider returns a s2s service for a valid token")

            .uponReceiving("0. Incorrect Authorization header")
            .path("/details")
            .method("GET")
            .headers("Authorization", "x")
            .willRespondWith()
            .status(403)

            .uponReceiving("1. Valid auth token")
            .path("/details")
            .method("GET")
            .matchHeader("Authorization", "(([a-z]|[A-Z]|[0-9])+)\\.(([a-z]|[A-Z]|[0-9])+)\\.(([a-z]|[A-Z]|[0-9])+)")
            .willRespondWith()
            .status(201)
            .body("em_gw")

            .uponReceiving("2. No auth token")
            .path("/details")
            .method("GET")
            .willRespondWith()
            .status(403)

            .toPact();
    }

    @Pact(provider = "s2s", consumer = "dm")
    public RequestResponsePact createPactForTesting(PactDslWithProvider builder) {
        return builder
            .uponReceiving("4. lease request (testing)")
            .path("/lease")
            .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .method("POST")
            .willRespondWith()
            .status(200)
            .body("s2s.s2s.s2s")

            .toPact();
    }

    @Test
    @PactVerification(value = "s2s", fragment = "createPact")
    public void runTest() {
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add("Authorization", "x");
        HttpEntity<?> httpEntity = new HttpEntity<>(null, requestHeaders);
        try {
            new RestTemplate().exchange(mockProvider.getUrl() + "/details", HttpMethod.GET, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            Assert.assertEquals(403, e.getRawStatusCode());
        }

        requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add("Authorization", "abc.abc.abc");
        httpEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity r = new RestTemplate().exchange(mockProvider.getUrl() + "/details", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(201, r.getStatusCodeValue());

        requestHeaders = new LinkedMultiValueMap<>();
        httpEntity = new HttpEntity<>(null, requestHeaders);
        try {
            new RestTemplate().exchange(mockProvider.getUrl() + "/details", HttpMethod.GET, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            Assert.assertEquals(403, e.getRawStatusCode());
        }

    }

    @Test
    @PactVerification(value = "s2s", fragment = "createPactForTesting")
    public void runTestToCreatePactForTesting() {
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> httpEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity r = new RestTemplate().exchange(mockProvider.getUrl() + "/lease", HttpMethod.POST, httpEntity, String.class);
        Assert.assertEquals(200, r.getStatusCodeValue());
    }

}
