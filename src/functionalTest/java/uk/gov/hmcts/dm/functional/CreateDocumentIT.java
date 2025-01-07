package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import net.serenitybdd.annotations.Pending;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.util.MimeTypeUtils.IMAGE_GIF_VALUE;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG_VALUE;
import static uk.gov.hmcts.dm.functional.V1MimeTypes.IMAGE_BMP_VALUE;
import static uk.gov.hmcts.dm.functional.V1MimeTypes.IMAGE_TIF_VALUE;

public class CreateDocumentIT extends BaseIT {

    private static final String FILES_CONST = "files";
    private static final String ROLES_CONST = "roles";
    private static final String CLASSIFICATION_CONST = "classification";
    private static final String CITIZEN_CONST = "citizen";
    private static final String CASEWORKER_ROLE_CONST = "caseworker";
    private static final String ERROR_CONST = "error";
    private static final String DOCUMENTS_PATH = "/documents";
    private static final String PAST_TTL = "2018-10-31T10:10:10+0000";
    private static final String DISALLOWED_FILE_ERROR = "Your upload contains a disallowed file type";

    private static final String EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME
        = "_embedded.documents[0].originalDocumentName";
    private static final String EMBEDDED_DOCUMENTS_0_MIMETYPE = "_embedded.documents[0].mimeType";
    private static final String EMBEDDED_DOCUMENTS_0_CLASSIFICATION = "_embedded.documents[0].classification";
    private static final String EMBEDDED_DOCUMENTS_0_ROLES_0 = "_embedded.documents[0].roles[0]";
    private static final String EMBEDDED_DOCUMENTS_1_ORIGINALDOCUMENTNAME
        = "_embedded.documents[1].originalDocumentName";
    private static final String EMBEDDED_DOCUMENTS_0_LINKS_SELF_HREF = "_embedded.documents[0]._links.self.href";



    @Test
    public void cd1R1AsAuthenticatedUserUpload7FilesWithCorrectClassificationAndSomeRolesSet() throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment7Png()), MediaType.IMAGE_PNG_VALUE)
            .multiPart(FILES_CONST, file(getAttachment8Tif()), IMAGE_TIF_VALUE)
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(FILES_CONST, file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart(FILES_CONST, file(getAttachment25Tiff()), IMAGE_TIF_VALUE)
            .multiPart(FILES_CONST, file(getAttachment26Bmp()), IMAGE_BMP_VALUE)
            .multiPart(FILES_CONST, file(getAttachment27Jpeg()), IMAGE_JPEG_VALUE)
            .multiPart(FILES_CONST, file(getWord()),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart(FILES_CONST, file(getWordTemplate()),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            .multiPart(FILES_CONST, file(getExcel()),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .multiPart(FILES_CONST, file(getExcelTemplate()),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
            .multiPart(FILES_CONST, file(getPowerPoint()),
                "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .multiPart(FILES_CONST, file(getPowerPointTemplate()),
                "application/vnd.openxmlformats-officedocument.presentationml.template")
            .multiPart(FILES_CONST, file(getPowerPointSlideShow()),
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow")
            .multiPart(FILES_CONST, file(getWordOld()), "application/msword")
            .multiPart(FILES_CONST, file(getExcelOld()), "application/vnd.ms-excel")
            .multiPart(FILES_CONST, file(getPowerPointOld()), "application/vnd.ms-powerpoint")
            .multiPart(FILES_CONST, file(getTextAttachment1()), "text/plain")
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST).multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart("ttl", PAST_TTL)
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo(getAttachment7Png()))
            .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_PNG_VALUE))
            .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body(EMBEDDED_DOCUMENTS_0_ROLES_0, equalTo(CASEWORKER_ROLE_CONST))
            .body("_embedded.documents[0].roles[1]", equalTo(CITIZEN_CONST))
            .body(EMBEDDED_DOCUMENTS_1_ORIGINALDOCUMENTNAME, equalTo(getAttachment8Tif()))
            .body("_embedded.documents[1].mimeType", equalTo(IMAGE_TIF_VALUE))
            .body("_embedded.documents[1].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[1].roles[0]", equalTo(CASEWORKER_ROLE_CONST))
            .body("_embedded.documents[1].roles[1]", equalTo(CITIZEN_CONST))
            .body("_embedded.documents[2].originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("_embedded.documents[2].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[2].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[2].roles[0]", equalTo(CASEWORKER_ROLE_CONST))
            .body("_embedded.documents[2].roles[1]", equalTo(CITIZEN_CONST))
            .body("_embedded.documents[3].originalDocumentName", equalTo(getAttachment4Pdf()))
            .body("_embedded.documents[3].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[4].originalDocumentName", equalTo(getAttachment25Tiff()))
            .body("_embedded.documents[4].mimeType", equalTo(IMAGE_TIF_VALUE))
            .body("_embedded.documents[5].originalDocumentName", equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[5].mimeType", equalTo(IMAGE_BMP_VALUE))
            .body("_embedded.documents[6].originalDocumentName", equalTo(getAttachment27Jpeg()))
            .body("_embedded.documents[6].mimeType", equalTo(IMAGE_JPEG_VALUE))
            .when()
            .post(DOCUMENTS_PATH);

        String documentUrl1 = replaceHttp(response.path(EMBEDDED_DOCUMENTS_0_LINKS_SELF_HREF));
        String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getAttachment7Png()))
            .body(CLASSIFICATION_CONST, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", equalTo(CASEWORKER_ROLE_CONST))
            .body("roles[1]", equalTo(CITIZEN_CONST))
            .when()
            .get(documentUrl1);

        assertByteArrayEquality(getAttachment7Png(),
            givenRequest(getCitizen())
                .expect()
                .statusCode(200)
                .contentType(equalTo(MediaType.IMAGE_PNG_VALUE))
                .header("OriginalFileName", getAttachment7Png())
                .when()
                .get(documentContentUrl1)
                .asByteArray());
    }

    @Test
    public void cd2AsUnauthenticatedUserIFail403ToUploadFiles() {
        givenUnauthenticatedRequest()
            .multiPart(FILES_CONST, file(getAttachment7Png()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart(FILES_CONST, file(getAttachment7Png()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(403)
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd3AsAuthenticatedUserIFailToUploadAFileWithoutClassification() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd4AsAuthenticatedUserIFailToUploadFilesWithIncorrectClassification() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(CLASSIFICATION_CONST, "XYZ")
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd5AsAuthenticatedUserIFailedWhenITriedToMakeAPostRequestWithoutAnyFile() {
        givenRequest(getCitizen())
            .multiPart(CLASSIFICATION_CONST, Classifications.RESTRICTED)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Provide some files to be uploaded."))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd6AsAuthenticatedUserIUploadFileWithNameContainingIllegalCharacters() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getIllegalNameFile()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(FILES_CONST, file(getIllegalNameFile1()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(FILES_CONST, file(getIllegalNameFile2()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo("uploadFile.jpg"))
            .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body(EMBEDDED_DOCUMENTS_0_ROLES_0, equalTo(CASEWORKER_ROLE_CONST))
            .body(EMBEDDED_DOCUMENTS_1_ORIGINALDOCUMENTNAME, equalTo("uploadFile_-.jpg"))
            .body("_embedded.documents[2].originalDocumentName", equalTo("uploadFile9 _-.jpg"))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd7AsAuthenticatedUserICanUploadFilesOfDifferentFormat() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(FILES_CONST, file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200).contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo(getAttachment9Jpg()))
            .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[0].roles", equalTo(null))
            .body(EMBEDDED_DOCUMENTS_1_ORIGINALDOCUMENTNAME, equalTo(getAttachment4Pdf()))
            .body("_embedded.documents[1].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[1].classification", equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[1].roles", equalTo(null))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    @Disabled("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void cd8AsAuthenticatedUserICanNotUploadFilesOfDifferentFormatIfNotOnTheWhitelistExe() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart(FILES_CONST, file(getBadAttachment1()), MediaType.ALL_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd9AsAuthenticatedUserICannotUploadXmlFile() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment18()), MediaType.APPLICATION_XML_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd10AsAuthenticatedUserICannotUploadSvgFile() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment10()), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd11R1AsAuthenticatedWhenIUploadAFileOnlyFirstTtlWillBeTakenIntoConsideration() {
        if (getToggleTtlEnabled()) {
            givenRequest(getCitizen())
                .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
                .multiPart(ROLES_CONST, CITIZEN_CONST).multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
                .multiPart("ttl", PAST_TTL)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo(getAttachment9Jpg()))
                .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_JPEG_VALUE))
                .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PUBLIC)))
                .body(EMBEDDED_DOCUMENTS_0_ROLES_0, equalTo(CASEWORKER_ROLE_CONST))
                .body("_embedded.documents[0].ttl", equalTo(PAST_TTL))
                .when()
                .post(DOCUMENTS_PATH);
        }

    }

    @Test
    @Pending
    public void cd12R1AsAUserWhenIUploadAFileWithATtlFileWillBeRemovedByBackgroundProcessOnceTtlIsComplete()
        throws InterruptedException {
        if (getToggleTtlEnabled()) {
            String url = givenRequest(getCitizen())
                .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
                .multiPart(ROLES_CONST, CITIZEN_CONST).multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo(getAttachment9Jpg()))
                .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_JPEG_VALUE))
                .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PUBLIC)))
                .body(EMBEDDED_DOCUMENTS_0_ROLES_0, equalTo(CASEWORKER_ROLE_CONST))
                .when()
                .post(DOCUMENTS_PATH)
                .path(EMBEDDED_DOCUMENTS_0_LINKS_SELF_HREF);

            int statusCode = -1;
            LocalDateTime start = LocalDateTime.now();

            while (statusCode != 404 && (Duration.between(start, LocalDateTime.now()).getSeconds() < 600)) {

                statusCode = givenRequest(getCitizen())
                    .expect()
                    .statusCode(Matchers.is(oneOf(404, 200)))
                    .when()
                    .get(url)
                    .statusCode();
                Thread.sleep(1000);
            }
            givenRequest(getCitizen())
                .expect()
                .statusCode(404)
                .when()
                .get(url);
        }

    }

    @Test
    public void cd16AsAUserIShouldNotBeAbleToUploadAnSvgIfItsRenamedToPdf() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getSvgAsPdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd17AsAUserIShouldNotBeAbleToUploadAnXmlIfItsRenamedToPdf() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getXmlAsPdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    @Disabled("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void cd18AsAUserIShouldNotBeAbleToUploadAnExeIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getExeAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd19AsAUserIShouldNotBeAbleToUploadAnSvgIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getSvgAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd20AsAUserIShouldNotBeAbleToUploadAnXmlIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getXmlAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd21R1AsAuthenticatedUserIShouldNotBeAbleToUploadGif() {

        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment6Gif()), IMAGE_GIF_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd22AsAuthenticatedUserICannotUploadMacroEnabledWord() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getWordMacroEnabled()), "application/vnd.ms-word.document.macroEnabled.12")
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd23AsAuthenticatedUserICannotUploadMacroEnabledExcel() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getExcelTemplateMacroEnabled()),
                "application/vnd.ms-excel.template.macroEnabled.12")
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd24AsAuthenticatedUserICannotUploadMacroEnabledPowerPoint() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getPowerPointSlideShowMacroEnabled()),
                "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(DISALLOWED_FILE_ERROR))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd25AsAuthenticatedUserICanUploadWithXForwardHeaders() {
        String forwardedHost = "ccd-gateway.service.internal";
        givenRequest(getCitizen())
            .header("x-forwarded-host", forwardedHost)
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0]._links.binary.href", not(containsString(forwardedHost)))
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void cd26AsAUserIShouldBeAbleToUploadAFileWithATtl() {

        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .multiPart("ttl", "2021-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body(EMBEDDED_DOCUMENTS_0_ORIGINALDOCUMENTNAME, equalTo(getAttachment9Jpg()))
            .body(EMBEDDED_DOCUMENTS_0_MIMETYPE, equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body(EMBEDDED_DOCUMENTS_0_CLASSIFICATION, equalTo(String.valueOf(Classifications.PUBLIC)))
            .body(EMBEDDED_DOCUMENTS_0_ROLES_0, equalTo(CASEWORKER_ROLE_CONST))
            .body("_embedded.documents[0].ttl", equalTo("2021-01-31T10:10:10+0000"))
            .when()
            .post(DOCUMENTS_PATH)
            .path(EMBEDDED_DOCUMENTS_0_LINKS_SELF_HREF);
    }

    @Test
    public void uploadLessThanMinSizeFails() {
        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file("zerobytes.txt"), MediaType.TEXT_PLAIN_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_ROLE_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo("Your upload file size is less than allowed limit."))
            .when()
            .post(DOCUMENTS_PATH);
    }
}
