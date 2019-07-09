package uk.gov.hmcts.dm.pact;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class IdamConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("sidam", "localhost", 4501, this);

    @Pact(provider="sidam", consumer="dm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .given("provider returns a SIDAM user for a valid token")

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
                .body(new PactDslJsonBody()
                        .integerType("id", 42)
                        .minArrayLike("roles", 1, PactDslJsonRootValue.
                                stringMatcher("CASEWORKER", "CASEWORKER")))

                .uponReceiving("2. No auth token")
                .path("/details")
                .method("GET")
                .willRespondWith()
                .status(403)

                .uponReceiving("2. No auth token")
                .path("/details")
                .method("GET")
                .willRespondWith()
                .status(403)

                .toPact();
    }


    @Test
    @PactVerification(value = "sidam", fragment ="createPact")
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

//
//
//    @Pact(provider="sidam", consumer="dm")
//    public RequestResponsePact createPactForTesting(PactDslWithProvider builder) {
//        return builder
//                .given("provider returns a SIDAM user for a valid token")
//
//                .uponReceiving("authorize request")
//                .path("/oauth2/authorize")
//                .method("POST")
//                .matchHeader("Authorization", "(([a-z]|[A-Z]|[0-9])+)\\.(([a-z]|[A-Z]|[0-9])+)\\.(([a-z]|[A-Z]|[0-9])+)")
//                .willRespondWith()
//                .status(200)
//                .body("{\"access-token\": \"t.t.t\"}")
//
//
//                .toPact();
//    }
//
//    @Test
//    @PactVerification(value = "sidam", fragment = "createPactForTesting")
//    public void runTestToCreatePactForTesting() {
//        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
//        HttpEntity<?> httpEntity = httpEntity = new HttpEntity<>(null, requestHeaders);
//        ResponseEntity r = new RestTemplate().exchange(mockProvider.getUrl() + "/testing-support/accounts", HttpMethod.POST, httpEntity, String.class);
//        Assert.assertEquals(201, r.getStatusCodeValue());
//
//    }

}
