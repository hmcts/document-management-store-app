package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.dm.controller.Const.ACCEPT_HEADER;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.DUMMY_SERVICE_AUTHORIZATION_VALUE;
import static uk.gov.hmcts.dm.controller.Const.PUBLIC_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.SERVICE_AUTHORIZATION_HEADER;

public class StoredDocumentControllerMultipartConsumerTest extends BaseConsumerPactTest {

    public static final String APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_COLLECTION_V_1_HAL_JSON_CHARSET_UTF_8 =
        "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8";
    private static final String PROVIDER = "dm_store_stored_document_multipart_provider";
    private static final String CONSUMER = "dm_store_stored_document_multipart_consumer";
    public static final String TEXT_PLAIN = "text/plain";

    // Apache HttpClient generates a random multipart boundary per run, which makes the
    // generated pact differ between builds for the same commit - the broker then rejects
    // the publish with HTTP 409. Pinning the boundary keeps the pact byte-identical.
    private static final String BOUNDARY = "pactBoundaryStoredDocumentMultipart";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    private static final String FILE_PART_NAME = "files";
    private static final String FILE_NAME = "test-file.txt";
    private static final byte[] FILE_CONTENT = "Hello World".getBytes(StandardCharsets.UTF_8);
    private static final String ROLES_PART_NAME = "roles";
    private static final String ROLE_CITIZEN = "citizen";

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact uploadDocumentsPact(PactBuilder builder) {
        return builder
            .given("Can create Stored Documents from multipart upload")

            .expectsToReceiveHttpInteraction("POST request to upload documents", http -> http
                .withRequest(request -> request
                    .path("/documents")
                    .method("POST")
                    .header(SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE)
                    .header(ACCEPT_HEADER,
                        APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_COLLECTION_V_1_HAL_JSON_CHARSET_UTF_8)
                    .header(CONTENT_TYPE_HEADER_NAME, MULTIPART_CONTENT_TYPE)
                    .body(multipartBody(), MULTIPART_CONTENT_TYPE)
                )
                .willRespondWith(response -> response
                    .status(200)
                    .header(CONTENT_TYPE_HEADER_NAME,
                        APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_COLLECTION_V_1_HAL_JSON_CHARSET_UTF_8)
                    .body(buildUploadResponseDsl())
                )
            )
            .toPact();
    }

    /**
     * Builds the multipart payload with a fixed boundary. ISO-8859-1 is byte-preserving,
     * so the payload round-trips through String without corruption.
     */
    private static String multipartBody() {
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                .setBoundary(BOUNDARY)
                .addBinaryBody(FILE_PART_NAME, FILE_CONTENT, ContentType.create(TEXT_PLAIN), FILE_NAME)
                .addTextBody(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION, ContentType.create(TEXT_PLAIN))
                .addTextBody(ROLES_PART_NAME, ROLE_CITIZEN, ContentType.create(TEXT_PLAIN))
                .build();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);
            return out.toString(StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocumentsPact", providerName = PROVIDER)
    void testUploadDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_COLLECTION_V_1_HAL_JSON_CHARSET_UTF_8)
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            // Must match DTO: "files"
            .multiPart(FILE_PART_NAME, FILE_NAME, FILE_CONTENT, TEXT_PLAIN)
            // Must match DTO: "classification" and be one of PUBLIC/PRIVATE/RESTRICTED
            .multiPart(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION)
            // Must match DTO: "roles"
            .multiPart("roles", "citizen")
            .when()
            .post("/documents")
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.documents[0].classification", equalTo(PUBLIC_CLASSIFICATION))
            .body("_embedded.documents[0].createdBy", equalTo("test-user-1"))
            .body("_embedded.documents[1].classification", equalTo(PUBLIC_CLASSIFICATION))
            .body("_embedded.documents[1].createdBy", equalTo("test-user-2"));
    }


    private DslPart buildUploadResponseDsl() {
        return newJsonBody(root ->
            root.object("_embedded", embedded ->
                embedded.array("documents", docs -> {
                    // Document 1
                    docs.object(doc -> {
                        doc.stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION);
                        doc.stringType("createdBy", "test-user-1");
                        doc.stringMatcher("createdOn", "\\d{4}-\\d{2}-\\d{2}T.*Z?", "2024-01-01T12:00:00");
                        doc.object("_links", links ->
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/11111111-1111-1111-1111-111111111111"))
                        );
                    });
                    // Document 2
                    docs.object(doc -> {
                        doc.stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION);
                        doc.stringType("createdBy", "test-user-2");
                        doc.stringMatcher("createdOn", "\\d{4}-\\d{2}-\\d{2}T.*Z?", "2024-01-01T12:00:00");
                        doc.object("_links", links ->
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/22222222-2222-2222-2222-222222222222"))
                        );
                    });
                })
            )
        ).build();
    }
}
