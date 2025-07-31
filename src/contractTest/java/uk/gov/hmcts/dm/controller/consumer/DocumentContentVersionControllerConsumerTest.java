package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class DocumentContentVersionControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_document_content_version_provider";
    private static final String CONSUMER = "dm_store_document_content_version_consumer";

    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";
    private static final String PATH = "/documents/" + DOCUMENT_ID + "/versions";

    private static final byte[] FILE_BYTES;

    static {
        try {
            FILE_BYTES = Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("test-files/sample.pdf").toURI())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact addDocumentContentVersionPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Can add Document Content Version and associate it with a given Stored Document.")
            .uponReceiving("POST multipart request to upload a document version")
            .method("POST")
            .withFileUpload(
                "file",
                "sample.pdf",
                "application/pdf",
                FILE_BYTES
            )
            .path(PATH)
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "Accept", "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(201)
            .headers(Map.of(
                "Content-Type", "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8",
                "Location", "http://localhost/documents/" + DOCUMENT_ID + "/versions/abc123"
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "addDocumentContentVersionPact")
    void testAddDocumentVersion(MockServer mockServer) throws URISyntaxException, IOException {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.MULTIPART)
            .multiPart("file", "sample.pdf", FILE_BYTES, "application/pdf")
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "Accept", "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .when()
            .post(PATH)
            .then()
            .log().all()
            .statusCode(201)
            .body("mimeType", equalTo("application/pdf"))
            .body("originalDocumentName", equalTo("sample.pdf"))
            .body("_links.self.href", containsString("/documents/" + DOCUMENT_ID + "/versions/"))
            .body("_links.binary.href", containsString("/binary"))
            .body("_links.document.href", containsString("/documents/" + DOCUMENT_ID));
    }

    private DslPart buildResponseDsl() {
        return newJsonBody((body) -> {
            body
                .uuid("id")
                .stringType("mimeType", "application/pdf")
                .stringType("originalDocumentName", "sample.pdf")
                .stringType("createdBy", "test-user")
                .stringType("createdByService", "test-service")
                .numberType("size", 1024)
                .stringType("contentUri", "http://localhost/documents/" + DOCUMENT_ID + "/versions/abc123")
                .stringType("contentChecksum", "abc123checksum")
                .object("_links", links -> {
                    links
                        .object("self", self -> self.stringType("href", "http://localhost/documents/" + DOCUMENT_ID + "/versions/abc123"))
                        .object("binary", binary -> binary.stringType("href", "http://localhost/documents/" + DOCUMENT_ID + "/versions/abc123/binary"))
                        .object("document", document -> document.stringType("href", "http://localhost/documents/" + DOCUMENT_ID));
                });
        }).build();
    }
}
