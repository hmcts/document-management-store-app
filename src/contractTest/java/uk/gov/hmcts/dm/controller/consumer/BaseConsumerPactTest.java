package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("secrets:S8217")
public class BaseConsumerPactTest {

    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String SERVICE_AUTH_TOKEN = "Bearer someServiceAuthorizationToken";

    protected Map<String, String> getHeaders() {
        return Map.of(
            AUTHORIZATION, AUTH_TOKEN,
            "ServiceAuthorization", SERVICE_AUTH_TOKEN,
            "Content-Type", "application/json"
        );
    }
}
