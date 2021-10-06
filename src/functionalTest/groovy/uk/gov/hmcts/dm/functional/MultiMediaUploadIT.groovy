package uk.gov.hmcts.dm.functional

import io.restassured.response.Response
import net.thucydides.core.annotations.Pending
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

class MultiMediaUploadIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "MV1 (R1) As authenticated user I upload large multi media files"() {
        uploadWhitelistedLargeFileThenDownload(video52mbId, "mp4-52mb", "video/mp4")
        uploadWhitelistedLargeFileThenDownload(video111mbId, "mp4-111mb", "video/mp4")
    }

    @Test
    void "MV1 (R1) As authenticated user I upload multi media files"() {
        uploadWhitelistedSmallFileThenDownload("video_test.mp4", "video/mp4")
        uploadWhitelistedSmallFileThenDownload("audio_test.mp3", "audio/mpeg")
    }

    @Test
    @Pending
    // See https://tools.hmcts.net/jira/browse/EM-3029 for details.
    void "MV1 (R1) As authenticated user I should not be able to upload files that exceed permitted sizes"() {
        uploadingFileThrowsValidationSizeErrorMessage("516MB_video_mp4.mp4", "video/mp4")
        uploadingFileThrowsValidationSizeErrorMessage("367MB_word.doc", "application/msword")

    }

    @Ignore
    @Test
    void "MV1 (R1) As authenticated user I cannot upload not whitelisted multi media files"() {
        uploadNotWhitelistedFileThenDownload("video_test.mov", "video/quicktime")
        uploadNotWhitelistedFileThenDownload("video_test.avi", "video/x-msvideo")
        uploadNotWhitelistedFileThenDownload("video_test.mpg", "video/mpeg")
        uploadNotWhitelistedFileThenDownload("video_test.webm", "video/webm")
        uploadNotWhitelistedFileThenDownload("video_test.wmv", "video/x-ms-wmv")

        uploadNotWhitelistedFileThenDownload("audio_test.wav", "audio/vnd.wave")
        uploadNotWhitelistedFileThenDownload("audio_test.aac", "audio/x-aac")
        uploadNotWhitelistedFileThenDownload("audio_test.ogg", "audio/vorbis")
        uploadNotWhitelistedFileThenDownload("audio_test.wma", "audio/x-ms-wma")

    }

    private uploadWhitelistedLargeFileThenDownload(String doc, String metadataKey, String mimeType) {
        File file = largeFile(doc, metadataKey)
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file, mimeType)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(file.getName()))
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
            .body("originalDocumentName", equalTo(file.getName()))
            .body("classification", equalTo(Classifications.PUBLIC as String))
            .body("roles[0]", equalTo("caseworker"))
            .body("roles[1]", equalTo("citizen"))
            .when()
            .get(documentUrl1)

        assertLargeDocByteArrayEquality file, givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(containsString(mimeType))
            .header("OriginalFileName", file.getName())
            .when()
            .get(documentContentUrl1)
            .asByteArray()

        file.delete()
    }

    private uploadWhitelistedSmallFileThenDownload(String fileName, String mimeType) {
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(fileName), mimeType)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(fileName))
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
            .body("originalDocumentName", equalTo(fileName))
            .body("classification", equalTo(Classifications.PUBLIC as String))
            .body("roles[0]", equalTo("caseworker"))
            .body("roles[1]", equalTo("citizen"))
            .when()
            .get(documentUrl1)

        assertByteArrayEquality fileName, givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(containsString(mimeType))
            .header("OriginalFileName", fileName)
            .when()
            .get(documentContentUrl1)
            .asByteArray()
    }

    private uploadNotWhitelistedFileThenDownload(String filename, String mimeType) {
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(filename), mimeType)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    private uploadingFileThrowsValidationSizeErrorMessage(String filename, String mimeType) {
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(filename), mimeType)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload file size is more than allowed limit."))
            .when()
            .post("/documents")
    }

}
