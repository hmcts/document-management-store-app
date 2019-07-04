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
import org.springframework.web.client.RestTemplate;

public class IdamConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("sidam", "localhost", 4501, this);

    @Pact(provider="sidam", consumer="dm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .given("provider returns a SIDAM user")
                .uponReceiving("a request for a token")
                .path("/details")
                .method("GET")
                .matchHeader("Authorization", ".+")
                .willRespondWith()
                .status(201)
                .matchHeader("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("id", 42)
                        .minArrayLike("roles", 1, PactDslJsonRootValue.
                                stringMatcher("TESTER|DEVELOPER|SOLICITOR", "SOLICITOR")))
                .toPact();
    }

    @Test
    @PactVerification("sidam")
    public void runTest() {
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();

        requestHeaders.add("Authorization", "some-access-token");

        HttpEntity<?> httpEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity r = new RestTemplate().exchange(mockProvider.getUrl() + "/details", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(201, r.getStatusCodeValue());
    }

}
