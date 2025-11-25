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
import static uk.gov.hmcts.dm.controller.Const.ACCEPT_HEADER;
import static uk.gov.hmcts.dm.controller.Const.APPLICATION_OCTET_STREAM;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.DOCUMENTS_IN_URI;
import static uk.gov.hmcts.dm.controller.Const.DUMMY_SERVICE_AUTHORIZATION_VALUE;
import static uk.gov.hmcts.dm.controller.Const.PUBLIC_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.dm.controller.Const.TEST_USER;

public class StoredDocumentControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_provider";
    private static final String CONSUMER = "dm_store_stored_document_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private static final String PATH_BINARY = DOCUMENTS_IN_URI + DOCUMENT_ID + "/binary";

    private static final byte[] DOWNLOAD_CONTENT = new byte[]{
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
    };
    public static final String APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON_CHARSET_UTF_8 =
        "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8";

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact getStoredDocumentPact(PactDslWithProvider builder) {
        return builder
            .given("A Stored Document exists and can be retrieved by documentId")
            .uponReceiving("GET request for a stored document by id")
            .path(DOCUMENTS_IN_URI + DOCUMENT_ID)
            .method("GET")
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON_CHARSET_UTF_8
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                "Content-Type", APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON_CHARSET_UTF_8
            ))
            .body(buildResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getStoredDocumentPact")
    void testGetStoredDocument(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_V_1_HAL_JSON_CHARSET_UTF_8)
            .headers(Map.of(SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE))
            .when()
            .get(DOCUMENTS_IN_URI + DOCUMENT_ID)
            .then()
            .log().all()
            .statusCode(200)
            .body(BODY_FIELD_CLASSIFICATION, equalTo(PUBLIC_CLASSIFICATION))
            .body("createdBy", equalTo(TEST_USER))
            .body("_links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    private DslPart buildResponseDsl() {
        return newJsonBody(body ->
            body.stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION)
                .stringType("createdBy", TEST_USER)
                .stringType("createdOn", "2024-01-01T12:00:00Z")
                .object("_links", links ->
                    links.object("self", self ->
                        self.stringType("href", "http://localhost/documents/" + DOCUMENT_ID))
                )
        ).build();
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
                "user-id", TEST_USER,
                "user-roles", "citizen",
                "classification", PUBLIC_CLASSIFICATION,
                ACCEPT_HEADER, APPLICATION_OCTET_STREAM
            ))
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(
                Map.of(
                    HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM,
                    HttpHeaders.CONTENT_LENGTH, String.valueOf(DOWNLOAD_CONTENT.length),
                    HttpHeaders.CONTENT_DISPOSITION, "fileName=\"sample.pdf\"",
                    "OriginalFileName", "sample.pdf",
                    "data-source", "contentURI"
                )
            )
            .withBinaryData(DOWNLOAD_CONTENT, APPLICATION_OCTET_STREAM)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "downloadStoredDocumentBinaryPact", providerName = PROVIDER)
    void testDownloadStoredDocumentBinary(MockServer mockServer) {
        Response response = SerenityRest
            .given()
            .headers(Map.of(
                "ServiceAuthorization", "Bearer some-s2s-token",
                "user-id", TEST_USER,
                "user-roles", "citizen",
                "classification", PUBLIC_CLASSIFICATION,
                "Accept", APPLICATION_OCTET_STREAM
            ))
            .get(mockServer.getUrl() + PATH_BINARY);

        response.then()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_OCTET_STREAM);

        assertThat(response.asByteArray()).hasSize(DOWNLOAD_CONTENT.length);
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("fileName=\"sample.pdf\"");
        assertThat(response.getHeader("OriginalFileName"))
            .isEqualTo("sample.pdf");
        assertThat(response.getHeader("data-source"))
            .isEqualTo("contentURI");
    }
}
