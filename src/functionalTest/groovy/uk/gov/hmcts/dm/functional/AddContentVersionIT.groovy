package uk.gov.hmcts.dm.functional

import io.restassured.response.Response
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.springframework.http.MediaType
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo

class AddContentVersionIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "ACV1 As authenticated user who is an owner POST a new version of the content to an existing document then expect 201"() {

        def documentURL = createDocumentAndGetUrlAs CITIZEN

        def response = givenRequest(CITIZEN)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentURL)
            .thenReturn()

        def newVersionUrl = response.getHeader 'Location'

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl)

    }


    @Test
    void "ACV2 As authenticated user POST a new version of the content to a not existing document"() {

        givenRequest(CITIZEN)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post('/documents' + UUID.randomUUID())

    }

    @Test
    void "ACV3 As unauthenticated user POST a new version of the content to a not existing document"() {

        givenUnauthenticatedRequest()
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post('/documents/' + UUID.randomUUID())

    }

    @Test
    void "ACV4 As unauthenticated user POST a new version of the content to an existing document"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url)

    }

    @Test
    void "ACV5 As authenticated user who is an not an owner, POST a new version of the content to an existing document"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url)

    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing this test to fail in CI")
    void "ACV6 As authenticated user who is not an owner and is a case worker"() {

        def url = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CASE_WORKER)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url)

    }

    @Test
    void "ACV7 As authenticated user who is a case worker POST a new version of the content to a not existing document and expect 404"() {

        givenRequest(CASE_WORKER)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post("documents/${UUID.randomUUID()}")

    }

    @Test
    void "ACV8 As an authenticated user and the owner I should not be able to upload multiple new content versions then expect 201"() {

        def documentURL = createDocumentAndGetUrlAs CITIZEN
        def response = givenRequest(CITIZEN)
            .multiPart("file", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("file", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("file", file(ATTACHMENT_3), MediaType.TEXT_PLAIN_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentURL)
            .thenReturn()

        def newVersionUrl = response.getHeader 'Location'

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl)
    }

    @Test
    void "ACV9 As an authenticated user and the owner I should be able to upload new version of different format"() {

        def documentURL = createDocumentAndGetUrlAs CITIZEN
        def response = givenRequest(CITIZEN)
            .multiPart("file", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_4_PDF))
            .body("mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .when()
            .post(documentURL)
            .thenReturn()

        def newVersionUrl = response.getHeader 'Location'

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl)

    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    void "ACV10 As an authenticated user and the owner I should not be able to upload exes"() {

        def documentURL = createDocumentAndGetUrlAs CITIZEN
        givenRequest(CITIZEN)
            .multiPart("file", file(BAD_ATTACHMENT_1), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentURL)
    }

    @Test
    void "ACV11 As an authenticated user and the owner I should not be able to upload zip"() {

        def documentURL = createDocumentAndGetUrlAs CITIZEN
        givenRequest(CITIZEN)
            .multiPart("file", file(BAD_ATTACHMENT_2), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentURL)
    }

    @Test
    void "ACV12 As an owner I cannot update the TTL while adding a version to the document"() {
        if (toggleTtlEnabled) {

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
        }
    }
}
