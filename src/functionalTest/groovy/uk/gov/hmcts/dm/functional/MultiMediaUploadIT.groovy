package uk.gov.hmcts.dm.functional

import io.restassured.response.Response
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

@RunWith(SpringIntegrationSerenityRunner.class)
class MultiMediaUploadIT extends BaseIT {

    @Test
    void "MV1 (R1) As authenticated user I upload multi media files"() {
        uploadFileThenDownload("video_test.mp4", "video/mp4")
        uploadFileThenDownload("video_test.mov", "video/quicktime")
        uploadFileThenDownload("video_test.avi", "video/x-msvideo")
        uploadFileThenDownload("video_test.mpg", "video/mpeg")
        uploadFileThenDownload("video_test.webm", "video/webm")
        uploadFileThenDownload("video_test.wmv", "video/x-ms-wmv")

        uploadFileThenDownload("audio_test.mp3", "audio/mpeg")
        uploadFileThenDownload("audio_test.wav", "audio/vnd.wave")
        uploadFileThenDownload("audio_test.aac", "audio/x-aac")
        uploadFileThenDownload("audio_test.ogg", "audio/vorbis")
        uploadFileThenDownload("audio_test.wma", "audio/x-ms-wma")
        uploadFileThenDownload("audio_test.m4a", "audio/mp4")
    }

    private uploadFileThenDownload(String filename, String mimeType) {
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(filename), mimeType)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(filename))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", equalTo("citizen"))
            .when()
            .post("/documents")

        String documentUrl1 = response.path("_embedded.documents[0]._links.self.href")
        String documentContentUrl1 = response.path("_embedded.documents[0]._links.binary.href")

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(filename))
            .body("classification", equalTo(Classifications.PUBLIC as String))
            .body("roles[0]", equalTo("caseworker"))
            .body("roles[1]", equalTo("citizen"))
            .when()
            .get(documentUrl1)

        assertByteArrayEquality filename, givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(containsString(mimeType))
            .header("OriginalFileName", filename)
            .when()
            .get(documentContentUrl1)
            .asByteArray()
    }

}
