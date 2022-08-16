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
}
