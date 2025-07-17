package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class StoredDocumentDeleteControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_delete_document_provider";
    private static final String CONSUMER = "dm_store_delete_document_consumer";

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String DELETE_PATH = "/documents/" + DOCUMENT_ID;

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
}
