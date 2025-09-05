package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentSearchControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_search_provider";
    private static final String CONSUMER = "dm_store_stored_document_search_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private static final String PATH_BINARY = "/documents/" + DOCUMENT_ID + "/binary";

    private static final byte[] DOWNLOAD_CONTENT = new byte[]{
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
    };

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
                "Accept", "application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8"
            ))
            .body("{\"name\":\"caseId\",\"value\":\"12345\"}")
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8"
            ))
            .body(buildPagedResponseDsl("/documents/filter"))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "filterDocumentsPact")
    void testFilterDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.JSON)
            .accept("application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8")
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .body("{\"name\":\"caseId\",\"value\":\"12345\"}")
            .when()
            .post("/documents/filter")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.documents[0].classification", equalTo("PUBLIC"))
            .body("_embedded.documents[0].createdBy", equalTo("test-user"))
            .body("_embedded.documents[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
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
                "Accept", "application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8"
            ))

            .body(buildPagedResponseDsl("/documents/owned"))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "ownedDocumentsPact")
    void testOwnedDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept("application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8")
            .headers(Map.of("ServiceAuthorization", "Bearer some-s2s-token"))
            .when()
            .post("/documents/owned")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.documents[0].classification", equalTo("PUBLIC"))
            .body("_embedded.documents[0].createdBy", equalTo("test-user"))
            .body("_embedded.documents[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    private DslPart buildPagedResponseDsl(String uri) {
        return new PactDslJsonBody()
            .object("_embedded")
            .minArrayLike("documents", 1)
            .stringType("classification", "PUBLIC")
            .stringType("createdBy", "test-user")
            .stringType("createdOn", "2025-09-02T14:20:42+0000")
            .array("roles")
            .stringType("citizen")
            .closeArray()
            .object("_links")
            .object("self")
            .stringMatcher("href", ".*/documents/[0-9a-f\\-]+",
                "http://localhost/documents/" + DOCUMENT_ID)
            .closeObject()
            .closeObject()
            .closeArray() // properly closes the "documents" array
            .closeObject()
            .object("_links")
            .object("self")
            .stringMatcher("href", ".*" + uri,
                "http://localhost" + uri)
            .closeObject()
            .closeObject()
            .object("page")
            .numberValue("number", 0)
            .numberValue("size", 1)
            .numberValue("totalElements", 1)
            .numberValue("totalPages", 1)
            .closeObject();
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact downloadStoredDocumentBinaryPact(PactDslWithProvider builder) {
        return builder
            .given("A specific Document Content Version binary exists for a given Stored Document.")
            .uponReceiving("GET request to download a stored document binary")
            .path(PATH_BINARY)
            .method("GET")
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "user-id", "test-user",
                "user-roles", "citizen",
                "classification", "PUBLIC",
                "Accept", "application/octet-stream"
            ))
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(
                Map.of(
                    HttpHeaders.CONTENT_TYPE, "application/octet-stream",
                    HttpHeaders.CONTENT_LENGTH, String.valueOf(DOWNLOAD_CONTENT.length),
                    HttpHeaders.CONTENT_DISPOSITION, "fileName=\"sample.pdf\"",
                    "OriginalFileName", "sample.pdf",
                    "data-source", "contentURI"
                )
            )
            .withBinaryData(DOWNLOAD_CONTENT, "application/octet-stream")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "downloadStoredDocumentBinaryPact", providerName = PROVIDER)
    void testDownloadStoredDocumentBinary(MockServer mockServer) {
        Response response = SerenityRest
            .given()
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "user-id", "test-user",
                "user-roles", "citizen",
                "classification", "PUBLIC",
                "Accept", "application/octet-stream"
            ))
            .get(mockServer.getUrl() + PATH_BINARY);

        response.then()
            .statusCode(HttpStatus.OK.value())
            .contentType("application/octet-stream");

        assertThat(response.asByteArray()).hasSize(DOWNLOAD_CONTENT.length);
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("fileName=\"sample.pdf\"");
        assertThat(response.getHeader("OriginalFileName"))
            .isEqualTo("sample.pdf");
        assertThat(response.getHeader("data-source"))
            .isEqualTo("contentURI");
    }
}
