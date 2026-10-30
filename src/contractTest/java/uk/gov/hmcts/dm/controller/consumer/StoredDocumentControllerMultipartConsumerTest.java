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

    // Apache HttpClient generates a random multipart boundary per run, which made the
    // published pact differ between builds for the same commit and the broker reject it
    // with HTTP 409. The boundary is pinned here, and both the pact expectation and the
    // request the test sends are built from the same bytes so they match exactly.
    private static final String BOUNDARY = "pactBoundaryStoredDocumentMultipart";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    private static final String FILE_PART_NAME = "files";
    private static final String FILE_NAME = "test-file.txt";
    private static final byte[] FILE_CONTENT = "Hello World".getBytes(StandardCharsets.UTF_8);
    private static final String ROLES_PART_NAME = "roles";
    private static final String ROLE_CITIZEN = "citizen";

    /**
     * Multipart payload with a fixed boundary. Used for both the pact expectation and the
     * request the test actually sends, so the two are byte-identical and match on equality.
     */
    private static byte[] multipartBody() {
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                .setBoundary(BOUNDARY)
                .addBinaryBody(FILE_PART_NAME, FILE_CONTENT, ContentType.create(TEXT_PLAIN), FILE_NAME)
                .addTextBody(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION, ContentType.create(TEXT_PLAIN))
                .addTextBody(ROLES_PART_NAME, ROLE_CITIZEN, ContentType.create(TEXT_PLAIN))
                .build();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * ISO-8859-1 is byte-preserving, so the payload round-trips through String intact.
     * body(String, contentType) is used rather than withBinaryData(...) because the latter
     * installs a ContentTypeMatcher that sniffs the payload and never matches multipart.
     */
    private static String multipartBodyAsString() {
        return new String(multipartBody(), StandardCharsets.ISO_8859_1);
    }

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
                    .body(multipartBodyAsString(), MULTIPART_CONTENT_TYPE)
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

    @Test
    @PactTestFor(pactMethod = "uploadDocumentsPact", providerName = PROVIDER)
    void testUploadDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_COLLECTION_V_1_HAL_JSON_CHARSET_UTF_8)
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            .contentType(MULTIPART_CONTENT_TYPE)
            .body(multipartBody())
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
                                self.stringType("href", "http://localhost/documents/0e72e5df-ef4e-42ae-a9e3-968601586336"))
                        );
                    });
                    // Document 2
                    docs.object(doc -> {
                        doc.stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION);
                        doc.stringType("createdBy", "test-user-2");
                        doc.stringMatcher("createdOn", "\\d{4}-\\d{2}-\\d{2}T.*Z?", "2024-01-01T12:00:00");
                        doc.object("_links", links ->
                            links.object("self", self ->
                                self.stringType("href", "http://localhost/documents/48f76e57-92b6-41ce-9df8-8235273b7b95"))
                        );
                    });
                })
            )
        ).build();
    }
}
