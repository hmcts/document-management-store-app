package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.exception.UncheckedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.controller.Const;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.dm.controller.Const.ACCEPT_HEADER;
import static uk.gov.hmcts.dm.controller.Const.APPLICATION_OCTET_STREAM;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_MIME_TYPE;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_ORIGINAL_DOCUMENT_NAME;
import static uk.gov.hmcts.dm.controller.Const.CONTENT_TYPE;
import static uk.gov.hmcts.dm.controller.Const.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.dm.controller.Const.DOCUMENTS_IN_URI;
import static uk.gov.hmcts.dm.controller.Const.DOCUMENT_NAME;
import static uk.gov.hmcts.dm.controller.Const.DUMMY_SERVICE_AUTHORIZATION_VALUE;
import static uk.gov.hmcts.dm.controller.Const.LOCATION_HEADER;
import static uk.gov.hmcts.dm.controller.Const.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.dm.controller.Const.VERSIONS_IN_URI;

public class DocumentContentVersionControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_document_content_version_provider";
    private static final String CONSUMER = "dm_store_document_content_version_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";
    private static final String DOCUMENT_CONTENT_VERSION_ID = "2216a872-81f7-4cad-a474-32a59608b038";
    private static final String PATH_VERSIONS = DOCUMENTS_IN_URI + DOCUMENT_ID + "/versions";
    private static final String PATH_LEGACY_ENDPOINT = DOCUMENTS_IN_URI + DOCUMENT_ID;

    private static final String PATH_GET_CONTENT =
        DOCUMENTS_IN_URI + DOCUMENT_ID + VERSIONS_IN_URI + DOCUMENT_CONTENT_VERSION_ID;

    private static final String PATH_DOCUMENT_CONTENT_VERSION_BINARY =
        DOCUMENTS_IN_URI + DOCUMENT_ID + VERSIONS_IN_URI + DOCUMENT_CONTENT_VERSION_ID + Const.BINARY;

    private static final byte[] DOWNLOAD_CONTENT = new byte[]{
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
    };

    private static final byte[] FILE_BYTES;

    static {
        try {
            FILE_BYTES = Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("test-files/sample.pdf").toURI())
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new UncheckedException(e);
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
                DOCUMENT_NAME,
                CONTENT_TYPE,
                FILE_BYTES
            )
            .path(PATH_VERSIONS)
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(201)
            .headers(Map.of(
                CONTENT_TYPE_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8",
                LOCATION_HEADER, "http://localhost/documents/" + DOCUMENT_ID + "/versions/" + DOCUMENT_CONTENT_VERSION_ID
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "addDocumentContentVersionPact")
    void testAddDocumentVersion(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.MULTIPART)
            .multiPart("file", DOCUMENT_NAME, FILE_BYTES, CONTENT_TYPE)
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .when()
            .post(PATH_VERSIONS)
            .then()
            .log().all()
            .statusCode(201)
            .body(BODY_FIELD_MIME_TYPE, equalTo(CONTENT_TYPE))
            .body(BODY_FIELD_ORIGINAL_DOCUMENT_NAME, equalTo(DOCUMENT_NAME))
            .body("_links.self.href", containsString(DOCUMENTS_IN_URI + DOCUMENT_ID + VERSIONS_IN_URI))
            .body("_links.binary.href", containsString(Const.BINARY))
            .body("_links.document.href", containsString(DOCUMENTS_IN_URI + DOCUMENT_ID));
    }



    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact addDocumentContentVersionLegacyMappingPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Can add Document Content Version and associate it with a given Stored Document.")
            .uponReceiving("POST multipart request to upload a document version using legacy endpoint")
            .method("POST")
            .withFileUpload(
                "file",
                DOCUMENT_NAME,
                CONTENT_TYPE,
                FILE_BYTES
            )
            .path(PATH_LEGACY_ENDPOINT)
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(201)
            .headers(Map.of(
                CONTENT_TYPE_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8",
                LOCATION_HEADER, "http://localhost/documents/" + DOCUMENT_ID + VERSIONS_IN_URI + DOCUMENT_CONTENT_VERSION_ID
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "addDocumentContentVersionLegacyMappingPact")
    void testAddDocumentVersionLegacy(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.MULTIPART)
            .multiPart("file", DOCUMENT_NAME, FILE_BYTES, CONTENT_TYPE)
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .when()
            .post(PATH_LEGACY_ENDPOINT)
            .then()
            .log().all()
            .statusCode(201)
            .body(BODY_FIELD_MIME_TYPE, equalTo(CONTENT_TYPE))
            .body(BODY_FIELD_ORIGINAL_DOCUMENT_NAME, equalTo(DOCUMENT_NAME))
            .body("_links.self.href", containsString(DOCUMENTS_IN_URI + DOCUMENT_ID + VERSIONS_IN_URI))
            .body("_links.binary.href", containsString(Const.BINARY))
            .body("_links.document.href", containsString(DOCUMENTS_IN_URI + DOCUMENT_ID));
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact getDocumentContentVersionPact(PactDslWithProvider builder) {
        return builder
            .given("A specific Document Content Version exists for a given Stored Document.")
            .uponReceiving("GET request for a specific document content version")
            .path(PATH_GET_CONTENT)
            .method("GET")
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                CONTENT_TYPE_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getDocumentContentVersionPact")
    void testGetDocumentContentVersion(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, "application/vnd.uk.gov.hmcts.dm.documentContentVersion.v1+hal+json;charset=UTF-8"
            ))
            .when()
            .get(PATH_GET_CONTENT)
            .then()
            .log().all()
            .statusCode(200)
            .body(BODY_FIELD_MIME_TYPE, equalTo(CONTENT_TYPE))
            .body(BODY_FIELD_ORIGINAL_DOCUMENT_NAME, equalTo(DOCUMENT_NAME))
            .body("_links.self.href", containsString(PATH_GET_CONTENT))
            .body("_links.binary.href", containsString(Const.BINARY))
            .body("_links.document.href", containsString("/documents/" + DOCUMENT_ID));
    }

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact downloadDocumentVersionBinaryPact(PactDslWithProvider builder) {
        return builder
            .given("A specific Document Content Version binary exists for a given Stored Document.")
            .uponReceiving("GET request to download a specific document content version binary")
            .path(PATH_DOCUMENT_CONTENT_VERSION_BINARY)
            .method("GET")
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, APPLICATION_OCTET_STREAM
            ))
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(
                Map.of(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM,
                    HttpHeaders.CONTENT_LENGTH, String.valueOf(DOWNLOAD_CONTENT.length),
                    HttpHeaders.CONTENT_DISPOSITION, "fileName=\"sample.pdf\"",
                    "OriginalFileName", DOCUMENT_NAME,
                    "data-source", "contentURI"
                )
            )
            .withBinaryData(DOWNLOAD_CONTENT, APPLICATION_OCTET_STREAM)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "downloadDocumentVersionBinaryPact", providerName = PROVIDER)
    void testDownloadDocumentVersionBinary(MockServer mockServer) {
        testDownload(mockServer);
    }

    private void testDownload(MockServer mockServer) {
        Response response = SerenityRest
            .given()
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, APPLICATION_OCTET_STREAM
            ))
            .get(mockServer.getUrl() + PATH_DOCUMENT_CONTENT_VERSION_BINARY);

        response.then()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_OCTET_STREAM);

        assertThat(response.asByteArray()).hasSize(DOWNLOAD_CONTENT.length);
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("fileName=\"sample.pdf\"");
        assertThat(response.getHeader("OriginalFileName"))
            .isEqualTo(DOCUMENT_NAME);
        assertThat(response.getHeader("data-source"))
            .isEqualTo("contentURI");
    }


    private DslPart buildResponseDsl() {
        return newJsonBody(body ->
            body
                .stringType(BODY_FIELD_MIME_TYPE, CONTENT_TYPE)
                .stringType(BODY_FIELD_ORIGINAL_DOCUMENT_NAME, DOCUMENT_NAME)
                .stringType("createdBy", "test-user")
                .numberType("size", 1024)
                .object("_links", links ->
                    links
                        .object("self", self -> self.stringType("href",
                            "http://localhost/documents/" + DOCUMENT_ID
                                + VERSIONS_IN_URI + DOCUMENT_CONTENT_VERSION_ID))
                        .object("binary", binary -> binary.stringType("href",
                            "http://localhost/documents/" + DOCUMENT_ID
                                + VERSIONS_IN_URI + DOCUMENT_CONTENT_VERSION_ID + Const.BINARY))
                        .object("document",
                            document ->
                                document.stringType("href", "http://localhost/documents/" + DOCUMENT_ID))
                )
        ).build();
    }
}
