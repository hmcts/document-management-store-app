package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class StoredDocumentDeleteControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_delete_document_provider";
    private static final String CONSUMER = "dm_store_delete_document_consumer";

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String DELETE_PATH = "/documents/" + DOCUMENT_ID;
    private static final String SOFT_DELETE_PATH = "/documents/delete";

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact deleteDocumentPact(PactDslWithProvider builder) {

        PactDslResponse response = builder
            .given("Document exists and can be deleted")
            .uponReceiving("A DELETE request to delete a document")
            .path(DELETE_PATH)
            .method("DELETE")
            .headers(getHeaders())
            .willRespondWith()
            .status(HttpStatus.NO_CONTENT.value()); // 204

        return response.toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "deleteDocumentPact")
    void testDeleteDocument(MockServer mockServer) {
        RestAssured
            .given()
            .headers(getHeaders())
            .delete(mockServer.getUrl() + DELETE_PATH)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact deleteDocumentSoftPact(PactDslWithProvider builder) {

        PactDslJsonBody requestBody = new PactDslJsonBody()
            .stringValue("caseRef", "CASE-123");

        PactDslJsonBody responseBody = new PactDslJsonBody()
            .integerType("caseDocumentsFound", 5)
            .integerType("markedForDeletion", 4);

        return builder
            .given("Document exists and can be soft deleted")
            .uponReceiving("A DELETE request to soft delete a document")
            .method("POST")
            .path(SOFT_DELETE_PATH)
            .headers(getHeaders())
            .body(requestBody)
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(responseBody)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "deleteDocumentSoftPact")
    void deleteDocumentSoftPact(MockServer mockServer) {
        RestAssured
            .given()
            .headers(getHeaders())
            .delete(mockServer.getUrl() + SOFT_DELETE_PATH)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
