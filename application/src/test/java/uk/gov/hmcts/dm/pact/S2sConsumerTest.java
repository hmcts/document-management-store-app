package uk.gov.hmcts.dm.pact;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
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
import org.springframework.web.client.RestTemplate;

public class S2sConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("s2s", "localhost", 4502, this);

    @Pact(provider="s2s", consumer="dm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .uponReceiving("ExampleJavaConsumerPactRuleTest test interaction")
            .path("/details")
            .matchHeader("ServiceAuthorization", ".+")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body("em_gw")
            .toPact();
    }

    @Test
    @PactVerification("s2s")
    public void runTest() {
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();

        requestHeaders.add("ServiceAuthorization", "some-access-token");

        HttpEntity<?> httpEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity r = new RestTemplate().exchange(mockProvider.getUrl() + "/details", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, r.getStatusCodeValue());
    }

}
