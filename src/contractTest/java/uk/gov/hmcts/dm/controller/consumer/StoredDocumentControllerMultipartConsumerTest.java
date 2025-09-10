package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.MultipartBuilder;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class StoredDocumentControllerMultipartConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_multipart_provider";
    private static final String CONSUMER = "dm_store_stored_document_multipart_consumer";

    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact uploadDocumentsPact(PactBuilder builder) {
        return builder
            .given("Can create Stored Documents from multipart upload")

            .expectsToReceiveHttpInteraction("POST request to upload documents", http -> http
                .withRequest(request -> request
                    .path("/documents")
                    .method("POST")
                    .header("ServiceAuthorization", "Bearer some-s2s-token")
                    .header("Accept", "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8")
                    .body(new MultipartBuilder()
                        .binaryPart("files", "test-file.txt", "Hello World".getBytes(), "text/plain")
                        .textPart("classification", "PUBLIC", "text/plain")
                        .textPart("roles", "citizen", "text/plain")
                    )
                )
                .willRespondWith(response -> response
                    .status(200)
                    .header("Content-Type",
                        "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8")
                    .body("{}", "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8")
                )
            )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocumentsPact", providerName = PROVIDER)
    void testUploadDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json;charset=UTF-8")
            .header("ServiceAuthorization", "Bearer some-s2s-token")
            // Must match DTO: "files"
            .multiPart("files", "test-file.txt", "Hello World".getBytes(), "text/plain")
            // Must match DTO: "classification" and be one of PUBLIC/PRIVATE/RESTRICTED
            .multiPart("classification", "PUBLIC")
            // Must match DTO: "roles"
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
