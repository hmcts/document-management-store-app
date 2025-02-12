package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import net.serenitybdd.annotations.Pending;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class MultiMediaUploadIT extends BaseIT {

    private static final String FILES_CONST = "files";
    private static final String CLASSIFICATION_CONST = "classification";
    private static final String ROLES_CONST = "roles";
    private static final String CITIZEN_CONST = "citizen";
    private static final String CASEWORKER_CONST = "caseworker";
    private static final String ERROR_CONST = "error";
    private static final String DOCUMENTS_PATH = "/documents";
    private static final String MIME_TYPE_MP4 = "video/mp4";

    @Test
    public void mv1R1AsAuthenticatedUserIUploadLargeMultiMediaFiles() throws IOException {
        assumeFalse(isDropBoxFile());
        uploadWhitelistedLargeFileThenDownload(getVideo52mbId(), "mp4-52mb", MIME_TYPE_MP4);
        uploadWhitelistedLargeFileThenDownload(getVideo111mbId(), "mp4-111mb", MIME_TYPE_MP4);
    }

    @Test
    public void mv1R1AsAuthenticatedUserIUploadMultiMediaFiles() throws IOException {
        uploadWhitelistedSmallFileThenDownload("video_test.mp4", MIME_TYPE_MP4);
        uploadWhitelistedSmallFileThenDownload("audio_test.mp3", "audio/mpeg");
        uploadWhitelistedSmallFileThenDownload("m4a.m4a", "audio/mp4");
    }

    @Test
    @Pending
    @Disabled
    public void mv1R1AsAuthenticatedUserIShouldNotBeAbleToUploadFilesThatExceedPermittedSizes() {
        uploadingFileThrowsValidationSizeErrorMessage("516MB_video_mp4.mp4", MIME_TYPE_MP4);
        uploadingFileThrowsValidationSizeErrorMessage("367MB_word.doc", "application/msword");

    }

    @Disabled
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

    @Test
    public void uploadWhiteListedWithPasswordThenFail() {
        uploadFileThrowsPasswordErrorMessage("pw_protected.pdf", "application/pdf");
        uploadFileThrowsPasswordErrorMessage("pw_protected_docx.docx", "application/msword");
    }

    private boolean uploadWhitelistedLargeFileThenDownload(String doc, String metadataKey, String mimeType)
        throws IOException {
        File file = largeFile(doc, metadataKey);
        Response response = givenRequest(getCitizen())
            .multiPart(FILES_CONST, file, mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(file.getName()))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo(CASEWORKER_CONST))
            .body("_embedded.documents[0].roles[1]", equalTo(CITIZEN_CONST))
            .when()
            .post(DOCUMENTS_PATH);

        String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));
        String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(file.getName()))
            .body(CLASSIFICATION_CONST, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", equalTo(CASEWORKER_CONST))
            .body("roles[1]", equalTo(CITIZEN_CONST))
            .when()
            .get(documentUrl1);

        assertLargeDocByteArrayEquality(file, givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(containsString(mimeType))
            .header("OriginalFileName", file.getName())
            .when()
            .get(documentContentUrl1)
            .asByteArray());

        Files.delete(file.toPath());
        return true;
    }

    private void uploadWhitelistedSmallFileThenDownload(String fileName, String mimeType) throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(fileName), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(fileName))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo(CASEWORKER_CONST))
            .body("_embedded.documents[0].roles[1]", equalTo(CITIZEN_CONST))
            .when()
            .post(DOCUMENTS_PATH);

        String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));
        String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(fileName))
            .body(CLASSIFICATION_CONST, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", equalTo(CASEWORKER_CONST))
            .body("roles[1]", equalTo(CITIZEN_CONST))
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
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(filename), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload contains a disallowed file type"))
            .when()
            .post(DOCUMENTS_PATH);
    }

    private void uploadingFileThrowsValidationSizeErrorMessage(String filename, String mimeType) {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(filename), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload file size is more than allowed limit."))
            .when()
            .post(DOCUMENTS_PATH);
    }

    private void uploadFileThrowsPasswordErrorMessage(String filename, String mimeType) {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(filename), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload file is password protected."))
            .when()
            .post(DOCUMENTS_PATH);
    }


}
