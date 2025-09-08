package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_provider";
    private static final String CONSUMER = "dm_store_stored_document_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private static final String PATH_BINARY = "/documents/" + DOCUMENT_ID + "/binary";

    private static final byte[] DOWNLOAD_CONTENT = new byte[]{
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
    };

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


    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact uploadDocumentsPact(PactDslWithProvider builder) {
        return builder
            .given("Can create Stored Documents from multipart upload")
            .uponReceiving("POST request to upload documents")
            .path("/documents")
            .method("POST")
            .matchHeader("ServiceAuthorization", "Bearer .*", "Bearer some-s2s-token")
            .matchHeader(
                "Content-Type",
                "multipart/form-data; boundary=.*"
            )
            .headers(Map.of(
                "Accept", "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8"
            ))
            .body(buildUploadResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocumentsPact", providerName = PROVIDER)
    void testUploadDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8")
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            .multiPart("files", "test-file.txt", "Hello World".getBytes())
            .multiPart("classification", "PUBLIC")
            .multiPart("roles", "citizen")
            .when()
            .post("/documents")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.storedDocumentHalResources[0].classification", equalTo("PUBLIC"))
            .body("_embedded.storedDocumentHalResources[0].createdBy", equalTo("test-user-1"))
            .body("_embedded.storedDocumentHalResources[1].classification", equalTo("PUBLIC"))
            .body("_embedded.storedDocumentHalResources[1].createdBy", equalTo("test-user-2"));
    }

    private DslPart buildUploadResponseDsl() {
        return newJsonBody(root -> {
            root.object("_embedded", embedded -> {
                embedded.array("storedDocumentHalResources", docs -> {
                    // Document 1
                    docs.object(doc -> {
                        doc.stringType("classification", "PUBLIC");
                        doc.stringType("createdBy", "test-user-1");
                        doc.stringMatcher("createdOn", "\\d{4}-\\d{2}-\\d{2}T.*Z", "2024-01-01T12:00:00Z");
                        doc.object("_links", links -> {
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/11111111-1111-1111-1111-111111111111"));
                        });
                    });
                    // Document 2
                    docs.object(doc -> {
                        doc.stringType("classification", "PUBLIC");
                        doc.stringType("createdBy", "test-user-2");
                        doc.stringMatcher("createdOn", "\\d{4}-\\d{2}-\\d{2}T.*Z", "2024-01-01T12:00:00Z");
                        doc.object("_links", links -> {
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/22222222-2222-2222-2222-222222222222"));
                        });
                    });
                });
            });
        }).build();
    }

}
