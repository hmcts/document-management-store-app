package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_provider";
    private static final String CONSUMER = "dm_store_stored_document_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact getStoredDocumentPact(PactDslWithProvider builder) {
        return builder
            .given("A Stored Document exists and can be retrieved by documentId")
            .uponReceiving("GET request for a stored document by id")
            .path("/documents/" + DOCUMENT_ID)
            .method("GET")
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "Accept", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getStoredDocumentPact")
    void testGetStoredDocument(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept("application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8")
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .when()
            .get("/documents/" + DOCUMENT_ID)
            .then()
            .log().all()
            .statusCode(200)
            .body("classification", equalTo("PUBLIC"))
            .body("createdBy", equalTo("test-user"))
            .body("_links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    private DslPart buildResponseDsl() {
        return newJsonBody(body -> {
            body.stringType("classification", "PUBLIC")
                .stringType("createdBy", "test-user")
                .stringType("createdOn", "2024-01-01T12:00:00Z")
                .object("_links", links -> {
                    links.object("self", self ->
                        self.stringType("href", "http://localhost/documents/" + DOCUMENT_ID));
                });
        }).build();
    }
}
