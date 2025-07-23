package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

public class StoredDocumentAuditControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_audit_provider";
    private static final String CONSUMER = "dm_store_audit_consumer";

    private static final String DOCUMENT_ID = "00351f93-dff5-46fa-af0d-b40c2cafb47f";

    private static final String GET_AUDIT_PATH = "/documents/" + DOCUMENT_ID + "/auditEntries";


    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact getAuditPact(PactDslWithProvider builder) {
        return builder
            .given("Audit entries exist for a stored document")
            .uponReceiving("GET request for audit entries")
            .method("GET")
            .path(GET_AUDIT_PATH)
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/vnd.uk.gov.hmcts.dm.audit.v1+json"))
            .body(createAuditEntriesResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getAuditPact")
    void testGetAuditEntries(MockServer mockServer) {
        RestAssured
            .given()
            .baseUri(mockServer.getUrl())
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            .when()
            .get(GET_AUDIT_PATH)
            .then()
            .log().all()
            .statusCode(200);
    }


    private DslPart createAuditEntriesResponseDsl() {
        return newJsonBody(body -> body
            .minArrayLike("auditEntries", 1, this::buildAuditEntryDslObject)
        ).build();
    }

    private void buildAuditEntryDslObject(LambdaDslObject audit) {
        audit
            .stringType("action", "READ")
            .stringType("username", "user@example.com")
            .stringType("type", "StoredDocumentAuditEntry")
            .stringValue("recordedDateTime", "2025-07-22T10:00:00Z")
            .object("_links", links -> {
                links.object("document", docLink ->
                    docLink.stringValue("href", "http://localhost/documents/" + DOCUMENT_ID)
                );
            });
    }
}
