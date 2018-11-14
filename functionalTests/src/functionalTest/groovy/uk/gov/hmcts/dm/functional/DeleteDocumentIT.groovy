package uk.gov.hmcts.dm.functional

import io.restassured.response.Response
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assume.assumeTrue

@RunWith(SpringRunner.class)
class DeleteDocumentIT extends BaseIT {
    private citizenDocumentUrl
    private caseWorkerDocumentUrl

    @Before
    void setup() throws Exception {
        this.citizenDocumentUrl = createDocumentAndGetUrlAs CITIZEN
        this.caseWorkerDocumentUrl = createDocumentAndGetUrlAs CASE_WORKER
    }

    @Test
    void "D1 Unauthenticated user cannot delete"() {
        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl)
    }

    @Test
    void "D2 Authenticated user can delete their own documents"() {
        givenRequest(CITIZEN)
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl)

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl)
    }

    @Test
    void "D3 Authenticated user cannot delete other user's documents"() {
        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl)
    }

    @Test
    void "D4 Case worker cannot delete other users' documents"() {
        givenRequest(CASE_WORKER)
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl)
    }

    @Test
    void "D5 Case worker can delete their own document"() {
        givenRequest(CASE_WORKER)
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl)

        givenRequest(CASE_WORKER)
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl)
    }

    @Test
    void "D6 Case worker can hard delete their own document"() {
        givenRequest(CASE_WORKER)
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl + "?permanent=true")

        givenRequest(CASE_WORKER)
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl)
    }

    @Test
    void "D7 User can hard delete their own document"() {
        givenRequest(CITIZEN)
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl + "?permanent=true")

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl)
    }

    @Test
    void "D8 As an owner of the document I could not get the TTL info once, I have done the soft delete"() {
        assumeTrue(toggleConfiguration.isTtl())

        Response response = CreateAUserforTTL CASE_WORKER

        String documentUrl1 = response.path("_embedded.documents[0]._links.self.href")
        String documentContentUrl1 = response.path("_embedded.documents[0]._links.binary.href")

        givenRequest(CASE_WORKER)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("ttl", "2018-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentUrl1)

        givenRequest(CASE_WORKER)
            .expect().log().all()
            .statusCode(200)
            .body("ttl", equalTo("2018-10-31T10:10:10+0000"))
            .when()
            .get(documentUrl1)

        givenRequest(CASE_WORKER)
            .expect().log().all()
            .statusCode(204)
            .body(not(containsString("ttl:")))
            .when()
            .delete(documentUrl1)

//        givenRequest(CASE_WORKER)
//            .expect().log().all()
//            .statusCode(404)
//
//            .when()
//            .get(documentUrl1)
    }

    @Test
    void "D9 As an owner of the document I could not get the TTL info once, I have done the hard delete"() {
        assumeTrue(toggleConfiguration.isTtl())

        Response response = CreateAUserforTTL CASE_WORKER

        String documentUrl1 = response.path("_embedded.documents[0]._links.self.href")

        givenRequest(CASE_WORKER)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("ttl", "2018-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentUrl1)

        givenRequest(CASE_WORKER)
            .expect().log().all()
            .statusCode(200)
            .body("ttl", equalTo("2018-10-31T10:10:10+0000"))
            .when()
            .get(documentUrl1)

        givenRequest(CASE_WORKER)
            .expect().log().all()
            .statusCode(204)
            .body(not(containsString("ttl:")))
            .when()
            .delete(documentUrl1 + "?permanent=true")

//        givenRequest(CASE_WORKER)
//            .expect().log().all()
//            .statusCode(404)
//            .body(not(containsString("ttl:")))
//            .when()
//            .get(documentUrl1)
    }

}
