package uk.gov.hmcts.dm.prod.smoke

import io.restassured.response.Response
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.BaseIT
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

@RunWith(SpringRunner.class)
class ProdBlobStorageIT extends BaseIT{

    @Test
    void "Smoke test1 As authenticated user upload a file with correct classification and some roles set"() {

        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_7_PNG), MediaType.IMAGE_PNG_VALUE)

            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_7_PNG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_PNG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", equalTo("citizen"))

            .when()
            .post("/documents")

        String documentUrl1 = response.path("_embedded.documents[0]._links.self.href")
        String documentContentUrl1 = response.path("_embedded.documents[0]._links.binary.href")
        String document1Size = response.path("_embedded.documents[0].size")

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_7_PNG))
            .body("classification", equalTo(Classifications.PUBLIC as String))
            .body("roles[0]", equalTo("caseworker"))
            .body("roles[1]", equalTo("citizen"))
            .when()
            .get(documentUrl1)

        assertByteArrayEquality ATTACHMENT_7_PNG, givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(containsString(MediaType.IMAGE_PNG_VALUE))
            .header("OriginalFileName", ATTACHMENT_7_PNG)
            .when()
            .get(documentContentUrl1)
            .asByteArray()
    }

    @Test
    void "Smoke test2 As authenticated user who is an owner, can read owned documents"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        def doc = givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

        Assert.assertNotNull doc.asByteArray()
    }
}
