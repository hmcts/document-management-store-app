package uk.gov.hmcts.dm.functional

import io.restassured.http.ContentType
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.springframework.http.MediaType
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MimeTypes
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

class ErrorPageIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "EP1 As an unauthenticated web user trying to access a document, receive JSON error page with 403"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .get(documentUrl)

    }

    @Test
    void "EP2 As an authenticated user trying to access an unknown document, receive JSON error page with 404"() {

        givenRequest(CITIZEN)
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .when()
            .get('documents/XXX')

    }

    @Test
    void "EP3 As an authenticated user trying to access document dir, receive JSON error page with 405"() {

        givenRequest(CITIZEN)
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(405)
            .contentType(ContentType.JSON)
            .when()
            .get('documents/')
    }

    @Test
    void "EP4 As an authenticated user trying to post no document, receive JSON error page with 415"() {

        givenRequest(CITIZEN)
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .when()
            .post('documents/')
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    void "EP5 As an authenticated user trying to post bad attachment, receive JSON error page with 415"() {

        givenRequest(CITIZEN)
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(BAD_ATTACHMENT_1), MediaType.ALL_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post('documents/')
    }

    @Test
    void "EP6 As an authenticated user but not the owner of the file, post the newer version of the file, receive JSON error page with 403"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .post(url)
    }

    @Test
    void "EP7 As an unauthenticated api user trying to access a document with accept JSON, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .accept(ContentType.JSON)
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP8 As an unauthenticated api user trying to access a document with no accept header, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP9 As an unauthenticated api user trying to access a document with document accept header, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP10 As an authenticated user but not the owner of the file, post the newer version of the file, receive JSON error page with 403"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .accept(ContentType.JSON)
            .multiPart("file", file(ATTACHMENT_18), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post(url)
    }

    @Test
    void "EP11 As an authenticated web user trying to post no document, receive JSON error page with 422"() {

        givenRequest(CITIZEN)
            .accept(ContentType.XML)
            .multiPart("file", file(ATTACHMENT_18), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .post('documents/')
    }

    @Test
    void "EP12 As an authenticated user, when I post a SVG document I should get JSON response"() {

        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_10), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents")
    }

    @Test
    void "EP13 As an authenticated user, when I post a XML document I should get JSON response"() {

        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_18), V1MimeTypes.APPLICATION_XML_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents")
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    void "EP14 As an authenticated user, when I post a EXE document I should get JSON response"() {

        givenRequest(CITIZEN)
            .multiPart("files", file(BAD_ATTACHMENT_1), V1MimeTypes.ALL_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents")
    }

}
