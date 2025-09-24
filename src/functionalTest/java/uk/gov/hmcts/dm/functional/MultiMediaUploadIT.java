package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class MultiMediaUploadIT extends BaseIT {

    private static final String FILES_CONST = "files";
    private static final String CLASSIFICATION_CONST = "classification";
    private static final String ROLES_CONST = "roles";
    private static final String CITIZEN_CONST = "citizen";
    private static final String CASEWORKER_CONST = "caseworker";
    private static final String ERROR_CONST = "error";
    private static final String DOCUMENTS_PATH = "/documents";

    @Test
    public void mv1R1AsAuthenticatedUserICannotUploadNotWhitelistedMultiMediaFiles() {
        uploadNotWhitelistedFileThenDownload("video_test.mpg", "video/mpeg");
    }



    private void uploadWhitelistedSmallFileThenDownload(String fileName, String mimeType) throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(fileName), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect()
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
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload contains a disallowed file type"))
            .when()
            .post(DOCUMENTS_PATH);
    }

    private void uploadFileThrowsPasswordErrorMessage(String filename, String mimeType) {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(filename), mimeType)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload file is password protected."))
            .when()
            .post(DOCUMENTS_PATH);
    }


}
