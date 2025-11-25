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
import static uk.gov.hmcts.dm.controller.Const.ACCEPT_HEADER;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.dm.controller.Const.DATE_REGEX;
import static uk.gov.hmcts.dm.controller.Const.DUMMY_SERVICE_AUTHORIZATION_VALUE;
import static uk.gov.hmcts.dm.controller.Const.EXAMPLE_USER;
import static uk.gov.hmcts.dm.controller.Const.HTTP_LOCALHOST_DOCUMENTS_URL;
import static uk.gov.hmcts.dm.controller.Const.PUBLIC_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.SERVICE_AUTHORIZATION_HEADER;

public class StoredDocumentUpdateControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_update_document_provider";
    private static final String CONSUMER = "dm_store_update_document_consumer";
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String PATCH_PATH = "/documents";

    private static final String PATCH_SPECIFIC_PATH = "/documents/" + DOCUMENT_ID;

    private static final String TTL_ISO_FORMATTED;
    public static final String BODY_FIELD_METADATA = "metadata";
    public static final String DOC_TYPE_VALUE_EVIDENCE = "evidence";
    public static final String DUMMY_USER_ID = "some-user-id";
    public static final String DUMMY_CASE_ID_VALUE = "123456";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    public static final String USER_ID_HEADER = "user-id";
    public static final String APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON =
        "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json";

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
                CONTENT_TYPE_HEADER, APPLICATION_JSON_CONTENT_TYPE,
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                USER_ID_HEADER, DUMMY_USER_ID
            ))
            .body(requestBody())
            .willRespondWith()
            .status(200)
            .headers(Map.of(CONTENT_TYPE_HEADER, APPLICATION_JSON_CONTENT_TYPE))
            .body(responseBody)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateDocumentsPact")
    void testUpdateDocuments(MockServer mockServer) {
        RestAssured
            .given()
            .baseUri(mockServer.getUrl())
            .header(CONTENT_TYPE_HEADER, APPLICATION_JSON_CONTENT_TYPE)
            .header(SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE)
            .header(USER_ID_HEADER, DUMMY_USER_ID)
            .body(requestBody().getBody().toString())
            .when()
            .patch(PATCH_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("result", equalTo("Success"));
    }


    private DslPart requestBody() {
        return newJsonBody(body ->
            body
                .stringValue("ttl", TTL_ISO_FORMATTED)
                .minArrayLike("documents", 1, doc ->
                    doc
                        .uuid("documentId", DOCUMENT_ID)
                        .object(BODY_FIELD_METADATA, metadata -> {
                            metadata.stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION);
                            metadata.stringType("caseTypeId", "TEST");
                        })
                )
        ).build();
    }


    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact updateSpecificDocumentWithHalPact(PactDslWithProvider builder) {
        DslPart halResponse = newJsonBody(body -> {
            body.numberType("size", 2048);
            body.stringType("mimeType", "application/pdf");
            body.stringType("createdBy", EXAMPLE_USER);
            body.stringType("lastModifiedBy", EXAMPLE_USER);
            body.stringMatcher("createdOn", DATE_REGEX,
                "2025-07-29T17:24:59+0000");
            body.stringMatcher("modifiedOn", DATE_REGEX,
                "2025-03-29T17:24:59+0000");
            body.array("roles", roles -> {
                roles.stringValue("caseworker");
                roles.stringValue("citizen");
            });
            body.object(BODY_FIELD_METADATA, meta -> {
                meta.stringValue("docType", DOC_TYPE_VALUE_EVIDENCE);
                meta.stringValue("caseId", DUMMY_CASE_ID_VALUE);
            });
            body.stringMatcher("ttl", DATE_REGEX,
                "2025-07-30T17:24:59+0000");

            body.object("_links", links -> {
                links.object("self", self ->
                    self.stringType("href", HTTP_LOCALHOST_DOCUMENTS_URL + DOCUMENT_ID)
                );
                links.object("binary", binary ->
                    binary.stringType("href", HTTP_LOCALHOST_DOCUMENTS_URL + DOCUMENT_ID + "/binary")
                );
            });

            body.object("_embedded", embedded ->
                embedded.object("allDocumentVersions", allDocVersions ->
                    allDocVersions.object("_embedded", inner ->
                        inner.minArrayLike("documentVersions", 1, version -> {
                            version.numberType("size", 2048);
                            version.stringType("mimeType", "application/pdf");
                            version.nullValue("originalDocumentName");
                            version.stringType("createdBy", EXAMPLE_USER);
                            version.stringMatcher("createdOn",
                                DATE_REGEX,
                                "2025-07-29T17:24:59+0000");
                            version.object("_links", links -> {
                                links.object("document", docLink ->
                                    docLink.stringType("href",
                                        HTTP_LOCALHOST_DOCUMENTS_URL + DOCUMENT_ID));
                                links.object("self", selfLink ->
                                    selfLink.stringType("href",
                                        HTTP_LOCALHOST_DOCUMENTS_URL
                                            + DOCUMENT_ID + "/versions/some-version-id"));
                                links.object("binary", binLink ->
                                    binLink.stringType("href",
                                        HTTP_LOCALHOST_DOCUMENTS_URL
                                            + DOCUMENT_ID + "/versions/some-version-id/binary"));
                            });
                        })
                    )
                )
            );
        }).build();


        return builder
            .given("Document exist and can be updated with new TTL")
            .uponReceiving("PATCH request to update specific document with HAL response")
            .path(PATCH_SPECIFIC_PATH)
            .method("PATCH")
            .headers(Map.of(
                CONTENT_TYPE_HEADER, APPLICATION_JSON_CONTENT_TYPE,
                ACCEPT_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON,
                SERVICE_AUTHORIZATION_HEADER, "Bearer some-s2s-token",
                USER_ID_HEADER, DUMMY_USER_ID
            ))
            .body(singleDocumentRequestBody())
            .willRespondWith()
            .status(200)
            .headers(Map.of(CONTENT_TYPE_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON))
            .body(halResponse)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateSpecificDocumentWithHalPact")
    void testUpdateSpecificDocumentWithHal(MockServer mockServer) {
        RestAssured
            .given()
            .baseUri(mockServer.getUrl())
            .header(CONTENT_TYPE_HEADER, APPLICATION_JSON_CONTENT_TYPE)
            .header(ACCEPT_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON)
            .header(SERVICE_AUTHORIZATION_HEADER, "Bearer some-s2s-token")
            .header(USER_ID_HEADER, DUMMY_USER_ID)
            .body(singleDocumentRequestBody().getBody().toString())
            .when()
            .patch(PATCH_SPECIFIC_PATH)
            .then()
            .statusCode(200)
            .contentType(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON)
            .body("_links.self.href", equalTo(HTTP_LOCALHOST_DOCUMENTS_URL + DOCUMENT_ID))
            .body("metadata.caseId", equalTo(DUMMY_CASE_ID_VALUE))
            .body("metadata.docType", equalTo(DOC_TYPE_VALUE_EVIDENCE));
    }

    private DslPart singleDocumentRequestBody() {
        return LambdaDsl.newJsonBody(body ->
            body
                .stringValue("ttl", TTL_ISO_FORMATTED)
                .object(BODY_FIELD_METADATA, metadata -> {
                    metadata.stringValue(BODY_FIELD_CLASSIFICATION, "PRIVATE");
                    metadata.stringValue("caseId", DUMMY_CASE_ID_VALUE);
                    metadata.stringValue("docType", DOC_TYPE_VALUE_EVIDENCE);
                })
        ).build();
    }
}

