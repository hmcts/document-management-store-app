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

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentUpdateControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_update_document_provider";
    private static final String CONSUMER = "dm_store_update_document_consumer";
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String PATCH_PATH = "/documents";

    private static final String PATCH_SPECIFIC_PATH = "/documents/" + DOCUMENT_ID;

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
        DslPart responseBody = newJsonBody(body ->
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
            .body("result", equalTo("Success"));
    }


    private DslPart requestBody() {
        return newJsonBody(body -> {
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


    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact updateSpecificDocumentWithHalPact(PactDslWithProvider builder) {
        DslPart halResponse = newJsonBody(body -> {
            body.uuid("id", DOCUMENT_ID);
            body.stringType("createdBy", "user@example.com");
            body.stringType("lastModifiedBy", "user@example.com");
            body.stringType("mimeType", "application/pdf");
            body.numberType("size", 2048);
            body.array("roles", roles -> {
                roles.stringValue("caseworker");
                roles.stringValue("citizen");
            });
            body.object("metadata", meta -> {
                meta.stringValue("caseId", "123456");
                meta.stringValue("docType", "evidence");
            });
            body.object("_links", links -> {
                links.object("self", self ->
                    self.stringType("href", "http://localhost/documents/" + DOCUMENT_ID + "/metadata")
                );
                links.object("binary", binary ->
                    binary.stringType("href", "http://localhost/documents/" + DOCUMENT_ID + "/binary")
                );
            });
            body.object("_embedded", embedded ->
                embedded.minArrayLike("allDocumentVersions", 1, docVer -> {
                    docVer.uuid("id");
                    docVer.stringType("createdBy", "user@example.com");
                    docVer.stringType("mimeType", "application/pdf");
                    docVer.numberType("size", 2048);
                })
            );
        }).build();

        return builder
            .given("Document exist and can be updated with new TTL")
            .uponReceiving("PATCH request to update specific document with HAL response")
            .path(PATCH_SPECIFIC_PATH)
            .method("PATCH")
            .headers(Map.of(
                "Content-Type", "application/json",
                "Accept", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json",
                "ServiceAuthorization", "Bearer some-s2s-token",
                "user-id", "some-user-id"
            ))
            .body(singleDocumentRequestBody())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/hal+json"))
            .body(halResponse)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateSpecificDocumentWithHalPact")
    void testUpdateSpecificDocumentWithHal(MockServer mockServer) {
        RestAssured
            .given()
            .baseUri(mockServer.getUrl())
            .header("Content-Type", "application/json")
            .header("Accept", "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json")
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            .header("user-id", "some-user-id")
            .body(singleDocumentRequestBody().getBody().toString())
            .when()
            .patch(PATCH_SPECIFIC_PATH)
            .then()
            .statusCode(200)
            .contentType("application/hal+json")
            .body("_links.self.href", equalTo("http://localhost/documents/" + DOCUMENT_ID + "/metadata"))
            .body("metadata.caseId", equalTo("123456"))
            .body("metadata.docType", equalTo("evidence"));
    }

    private DslPart singleDocumentRequestBody() {
        return LambdaDsl.newJsonBody(body -> {
            body
                .stringValue("ttl", TTL_ISO_FORMATTED)
                .object("metadata", metadata -> {
                    metadata.stringValue("classification", "PRIVATE");
                    metadata.stringValue("caseId", "123456");
                    metadata.stringValue("docType", "evidence");
                });
        }).build();
    }
}

