package uk.gov.hmcts.dm.it

import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.it.utilities.Classifications
import uk.gov.hmcts.dm.it.utilities.V1MediaTypes
import uk.gov.hmcts.dm.it.utilities.V1MimeTypes

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

/**
 * Created by matej on 27/10/2017.
 */
@RunWith(SpringRunner.class)
class ErrorPageIT extends BaseIT {

    @Before
    void setup() throws Exception {
        createUser CITIZEN
    }

    @Test
    void "EP1 As an unauthenticated web user trying to access a document, receive HTML error page with 401"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest()
                .accept(ContentType.HTML)
                .expect()
                    .contentType(ContentType.HTML)
                    .body("html.head.title", equalTo("401 Error"))
                    .statusCode(401)
                .when()
                    .get(documentUrl)

    }

    @Test
    void "EP2 As an authenticated web user trying to access an unknown document, receive HTML error page with 404"() {

        givenRequest(CITIZEN)
                .accept(ContentType.HTML)
                .expect()
                    .body("html.head.title", equalTo("404 Error"))
                    .statusCode(404)
                .when()
                    .get('documents/XXX')

    }

    @Test
    void "EP3 As an authenticated web user trying to access document/, receive HTML error page with 405"() {

        givenRequest(CITIZEN)
                .accept(ContentType.HTML)
                .expect()
                .body("html.head.title", equalTo("405 Error"))
                .statusCode(405)
                .when()
                .get('documents/')
    }

    @Test
    void "EP4 As an authenticated web user trying to post no document, receive HTML error page with 415"() {

        givenRequest(CITIZEN)
                .accept(ContentType.HTML)
                .expect()
                .body("html.head.title", equalTo("415 Error"))
                .statusCode(415)
                .when()
                .post('documents/')
    }

    @Test
    void "EP5 As an authenticated web user trying to post bad attachment, receive HTML error page with 415"() {

        givenRequest(CITIZEN)
                .accept(ContentType.HTML)
                .multiPart("file", file(BAD_ATTACHMENT_1), MediaType.ALL_VALUE)
                .expect()
                .body("html.head.title", equalTo("422 Error"))
                .statusCode(422)
                .when()
                .post('documents/')
    }

    @Test
    void "EP6 As an authenticated web user but not the owner of the file, post the newer version of the file, receive HTML error page with 403"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        createUser CITIZEN_2

        givenRequest(CITIZEN_2)
                .accept(ContentType.HTML)
                .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
                .expect()
                .body("html.head.title", equalTo("403 Error"))
                .statusCode(403)
                .when()
                .post(url)
    }

    @Test
    void "EP7 As an unauthenticated api user trying to access a document with accept JSON, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest()
            .accept(ContentType.JSON)
            .expect()
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .statusCode(401)
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP8 As an unauthenticated api user trying to access a document with no accept header, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest()
            .expect()
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .statusCode(401)
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP9 As an unauthenticated api user trying to access a document with document accept header, receive JSON error"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest()
            .accept(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .expect()
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .statusCode(401)
            .when()
            .get(documentUrl)
    }

    @Test
    void "EP10 As an authenticated web user but not the owner of the file, post the newer version of the file, receive HTML error page with 403"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        createUser CITIZEN

        givenRequest(CITIZEN)
            .accept(ContentType.JSON)
            .multiPart("file", file(ATTACHMENT_18), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .body(not(containsString("<!DOCTYPE html>")))
            .statusCode(422)
            .when()
            .post(url)
    }

    @Test
    void "EP11 As an authenticated web user trying to post no document, receive HTML error page with 415"() {

        givenRequest(CITIZEN)
            .accept(ContentType.XML)
            .multiPart("file", file(ATTACHMENT_18), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .body(not(containsString("<!DOCTYPE html>")))
            .statusCode(406)
            .when()
            .post('documents/')
    }

//    @Test
//    void "EP37 As an unauthenticated api user trying to access a document with accept JSON, receive JSON error"() {
//
//        def documentUrl = createDocumentAndGetUrlAs CITIZEN
//
//        def path1 = givenRequest()
//            .accept(ContentType.JSON)
//            .expect()
//            .contentType(ContentType.JSON)
////            .body(containsString())
//            .statusCode(401)
//            .when()
//            .get(documentUrl).path(".")
//
//        System.out.println(path)
//    }

    @Test
    void "EP12 As an authenticated user, when I post a SVG document I should get JSON response"() {

        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_10), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
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
            .contentType(ContentType.JSON)
            .when()
            .post("/documents")
    }

}
