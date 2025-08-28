package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentSearchControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_search_provider";
    private static final String CONSUMER = "dm_store_stored_document_search_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    // Pact for /documents/filter
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact filterDocumentsPact(PactDslWithProvider builder) {
        return builder
            .given("Documents exist matching metadata search criteria-filter by metadata")
            .uponReceiving("POST request to search documents by metadata")
            .path("/documents/filter")
            .method("POST")
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "Content-Type", "application/json",
                "Accept", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .body("{\"metadata\":{\"caseId\":\"12345\"}}") // sample metadata search command
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .body(buildPagedResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "filterDocumentsPact")
    void testFilterDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.JSON)
            .accept("application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8")
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .body("{\"metadata\":{\"caseId\":\"12345\"}}")
            .when()
            .post("/documents/filter")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.storedDocumentHalResources[0].classification", equalTo("PUBLIC"))
            .body("_embedded.storedDocumentHalResources[0].createdBy", equalTo("test-user"))
            .body("_embedded.storedDocumentHalResources[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    // Pact for /documents/owned
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact ownedDocumentsPact(PactDslWithProvider builder) {
        return builder
            .given("Documents exist for the current user-owned search")
            .uponReceiving("POST request to search documents owned by current user")
            .path("/documents/owned")
            .method("POST")
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "Accept", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8"
            ))
            .body(buildPagedResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "ownedDocumentsPact")
    void testOwnedDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept("application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8")
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .when()
            .post("/documents/owned")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.storedDocumentHalResources[0].classification", equalTo("PUBLIC"))
            .body("_embedded.storedDocumentHalResources[0].createdBy", equalTo("test-user"))
            .body("_embedded.storedDocumentHalResources[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    // Build a sample HAL page response
    private DslPart buildPagedResponseDsl() {
        return newJsonBody(body -> {
            body.object("_embedded", embedded -> {
                embedded.minArrayLike("storedDocumentHalResources", 1, 1, doc -> {
                    doc.stringType("classification", "PUBLIC")
                        .stringType("createdBy", "test-user")
                        .stringType("createdOn", "2024-01-01T12:00:00Z")
                        .object("_links", links -> {
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/" + DOCUMENT_ID));
                        });
                });
            });
            body.object("_links", links -> {
                links.object("self", self ->
                    self.stringType("href", "http://localhost/documents"));
            });
            body.numberType("page.size", 20);
            body.numberType("page.totalElements", 1);
            body.numberType("page.totalPages", 1);
            body.numberType("page.number", 0);
        }).build();
    }
}
