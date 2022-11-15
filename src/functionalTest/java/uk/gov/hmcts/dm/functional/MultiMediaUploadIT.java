package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.Pending;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class MultiMediaUploadIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void mv1R1AsAuthenticatedUserIUploadLargeMultiMediaFiles() throws IOException {
        uploadWhitelistedLargeFileThenDownload(getVideo52mbId(), "mp4-52mb", "video/mp4", (doc, metadataKey) -> {
            try {
                return largeFile(doc, metadataKey);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        uploadWhitelistedLargeFileThenDownload(getVideo111mbId(), "mp4-111mb", "video/mp4", (doc, metadataKey) -> {
            try {
                return largeFile(doc, metadataKey);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void mv1R1AsAuthenticatedUserIUploadMultiMediaFiles() throws IOException {
        uploadWhitelistedSmallFileThenDownload("video_test.mp4", "video/mp4");
        uploadWhitelistedSmallFileThenDownload("audio_test.mp3", "audio/mpeg");
    }

    @Test
    @Pending
    public void mv1R1AsAuthenticatedUserIShouldNotBeAbleToUploadFilesThatExceedPermittedSizes() {
        uploadingFileThrowsValidationSizeErrorMessage("516MB_video_mp4.mp4", "video/mp4");
        uploadingFileThrowsValidationSizeErrorMessage("367MB_word.doc", "application/msword");

    }

    @Ignore
    @Test
    public void mv1R1AsAuthenticatedUserICannotUploadNotWhitelistedMultiMediaFiles() {
        uploadNotWhitelistedFileThenDownload("video_test.mov", "video/quicktime");
        uploadNotWhitelistedFileThenDownload("video_test.avi", "video/x-msvideo");
        uploadNotWhitelistedFileThenDownload("video_test.mpg", "video/mpeg");
        uploadNotWhitelistedFileThenDownload("video_test.webm", "video/webm");
        uploadNotWhitelistedFileThenDownload("video_test.wmv", "video/x-ms-wmv");

        uploadNotWhitelistedFileThenDownload("audio_test.wav", "audio/vnd.wave");
        uploadNotWhitelistedFileThenDownload("audio_test.aac", "audio/x-aac");
        uploadNotWhitelistedFileThenDownload("audio_test.ogg", "audio/vorbis");
        uploadNotWhitelistedFileThenDownload("audio_test.wma", "audio/x-ms-wma");

    }



    private void uploadWhitelistedSmallFileThenDownload(String fileName, String mimeType) throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart("files", file(fileName), mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(fileName))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", equalTo("citizen"))
            .when()
            .post("/documents");

        String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));
        String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(fileName))
            .body("classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", equalTo("caseworker"))
            .body("roles[1]", equalTo("citizen"))
            .when()
            .get(documentUrl1);

        assertByteArrayEquality(fileName,
            givenRequest(getCitizen())
                .expect()
                .statusCode(200)
                .contentType(containsString(mimeType))
                .header("OriginalFileName", fileName)
                .when()
                .get(documentContentUrl1)
                .asByteArray());
    }

    private void uploadNotWhitelistedFileThenDownload(String filename, String mimeType) {
        Response response = givenRequest(getCitizen())
            .multiPart("files", file(filename), mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    private void uploadingFileThrowsValidationSizeErrorMessage(String filename, String mimeType) {
        Response response = givenRequest(getCitizen())
            .multiPart("files", file(filename), mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload file size is more than allowed limit."))
            .when()
            .post("/documents");
    }
}
