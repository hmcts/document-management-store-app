package uk.gov.hmcts.dm.controller.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.dm.controller.Const.ACCEPT_HEADER;
import static uk.gov.hmcts.dm.controller.Const.BODY_FIELD_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.dm.controller.Const.DUMMY_SERVICE_AUTHORIZATION_VALUE;
import static uk.gov.hmcts.dm.controller.Const.PUBLIC_CLASSIFICATION;
import static uk.gov.hmcts.dm.controller.Const.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.dm.controller.Const.TEST_USER;

public class StoredDocumentSearchControllerConsumerTest extends BaseConsumerPactTest {

    private static final String PROVIDER = "dm_store_stored_document_search_provider";
    private static final String CONSUMER = "dm_store_stored_document_search_consumer";
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";
    public static final String DOCUMENTS_FILTER_PATH = "/documents/filter";
    public static final String APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8 =
        "application/vnd.uk.gov.hmcts.dm.document-page.v1+hal+json;charset=UTF-8";
    public static final String DOCUMENTS_OWNED_URI = "/documents/owned";

    // Pact for /documents/filter
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact filterDocumentsPact(PactDslWithProvider builder) {
        return builder
            .given("Documents exist matching metadata search criteria-filter by metadata")
            .uponReceiving("POST request to search documents by metadata")
            .path(DOCUMENTS_FILTER_PATH)
            .method("POST")
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                CONTENT_TYPE_HEADER, "application/json",
                ACCEPT_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8
            ))
            .body("{\"name\":\"caseId\",\"value\":\"12345\"}")
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                CONTENT_TYPE_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8
            ))
            .body(buildPagedResponseDsl(DOCUMENTS_FILTER_PATH))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "filterDocumentsPact")
    void testFilterDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .contentType(ContentType.JSON)
            .accept(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8)
            .headers(Map.of(SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE))
            .body("{\"name\":\"caseId\",\"value\":\"12345\"}")
            .when()
            .post(DOCUMENTS_FILTER_PATH)
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.documents[0].classification", equalTo(PUBLIC_CLASSIFICATION))
            .body("_embedded.documents[0].createdBy", equalTo(TEST_USER))
            .body("_embedded.documents[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    // Pact for /documents/owned
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    public V4Pact ownedDocumentsPact(PactDslWithProvider builder) {
        return builder
            .given("Documents exist for the current user-owned search")
            .uponReceiving("POST request to search documents owned by current user")
            .path(DOCUMENTS_OWNED_URI)
            .method("POST")
            .headers(Map.of(
                SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE,
                ACCEPT_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8
            ))
            .willRespondWith()
            .status(200)
            .headers(Map.of(
                CONTENT_TYPE_HEADER, APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8
            ))

            .body(buildPagedResponseDsl(DOCUMENTS_OWNED_URI))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "ownedDocumentsPact")
    void testOwnedDocuments(MockServer mockServer) {
        given()
            .baseUri(mockServer.getUrl())
            .accept(APPLICATION_VND_UK_GOV_HMCTS_DM_DOCUMENT_PAGE_V_1_HAL_JSON_CHARSET_UTF_8)
            .headers(Map.of(SERVICE_AUTHORIZATION_HEADER, DUMMY_SERVICE_AUTHORIZATION_VALUE))
            .when()
            .post(DOCUMENTS_OWNED_URI)
            .then()
            .log().all()
            .statusCode(200)
            .body("_embedded.documents[0].classification", equalTo(PUBLIC_CLASSIFICATION
            ))
            .body("_embedded.documents[0].createdBy", equalTo("test-user"))
            .body("_embedded.documents[0]._links.self.href", containsString("/documents/" + DOCUMENT_ID));
    }

    private DslPart buildPagedResponseDsl(String uri) {
        return new PactDslJsonBody()
            .object("_embedded")
            .minArrayLike("documents", 1)
            .stringType(BODY_FIELD_CLASSIFICATION, PUBLIC_CLASSIFICATION)
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
}
