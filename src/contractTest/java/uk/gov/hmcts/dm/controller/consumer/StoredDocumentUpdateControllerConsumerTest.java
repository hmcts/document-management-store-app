package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class StoredDocumentUpdateControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_update_document_provider";
    private static final String CONSUMER = "dm_store_update_document_consumer";
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String PATCH_PATH = "/documents";
    private static final String TTL_ISO_FORMATTED;

    static {
        LocalDateTime targetDateTime = LocalDateTime.of(2025, 12, 29, 8, 28, 27);
        Instant instant = targetDateTime.toInstant(ZoneOffset.UTC);
        TTL_ISO_FORMATTED = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
            .withZone(ZoneOffset.UTC)
            .format(instant);
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact updateDocumentsPact(PactDslWithProvider builder) {
        DslPart responseBody = LambdaDsl.newJsonBody(body ->
            body.stringType("result", "Success")
        ).build();
        return builder
            .given("Documents exist and can be updated with new TTL")
            .uponReceiving("PATCH request to update documents")
            .path(PATCH_PATH)
            .method("PATCH")
            .headers(Map.of(
                "Content-Type", "application/json",
                "ServiceAuthorization", "Bearer some-s2s-token",
                "user-id", "some-user-id"
            ))
            .body(requestBody())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(responseBody)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateDocumentsPact")
    void testUpdateDocuments(MockServer mockServer) {
        RestAssured
            .given()
            .baseUri(mockServer.getUrl())
            .header("Content-Type", "application/json")
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            .header("user-id", "some-user-id")
            .body(requestBody().getBody().toString())
            .when()
            .patch(PATCH_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("result", org.hamcrest.Matchers.equalTo("Success"));
    }


    private DslPart requestBody() {
        return LambdaDsl.newJsonBody(body -> {
            body
                .stringValue("ttl", TTL_ISO_FORMATTED)
                .minArrayLike("documents", 1, doc -> {
                    doc
                        .uuid("documentId", DOCUMENT_ID)
                        .object("metadata", metadata -> {
                            metadata.stringType("classification", "PUBLIC");
                            metadata.stringType("caseTypeId", "TEST");
                        });
                });
        }).build();
    }
}

