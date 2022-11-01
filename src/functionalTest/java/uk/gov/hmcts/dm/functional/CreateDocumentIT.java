package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import net.thucydides.core.annotations.Pending;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;

public class CreateDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void cd1R1AsAuthenticatedUserUpload7FilesWithCorrectClassificationAndSomeRolesSet() throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment7Png()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("files", file(getAttachment8Tif()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(getAttachment25Tiff()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("files", file(getAttachment27Jpeg()), V1MimeTypes.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getWord()), "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart("files", file(getWordTemplate()), "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            .multiPart("files", file(getExcel()), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .multiPart("files", file(getExcelTemplate()), "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
            .multiPart("files", file(getPowerPoint()), "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .multiPart("files", file(getPowerPointTemplate()), "application/vnd.openxmlformats-officedocument.presentationml.template")
            .multiPart("files", file(getPowerPointSlideShow()), "application/vnd.openxmlformats-officedocument.presentationml.slideshow")
            .multiPart("files", file(getWordOld()), "application/msword")
            .multiPart("files", file(getExcelOld()), "application/vnd.ms-excel")
            .multiPart("files", file(getPowerPointOld()), "application/vnd.ms-powerpoint")
            .multiPart("files", file(getTextAttachment1()), "text/plain")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen").multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment7Png()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_PNG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[1].originalDocumentName", Matchers.equalTo(getAttachment8Tif()))
            .body("_embedded.documents[1].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[1].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[1].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[1].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[2].originalDocumentName", Matchers.equalTo(getAttachment9Jpg()))
            .body("_embedded.documents[2].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[2].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[2].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[2].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[3].originalDocumentName", Matchers.equalTo(getAttachment4Pdf()))
            .body("_embedded.documents[3].mimeType", Matchers.equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[4].originalDocumentName", Matchers.equalTo(getAttachment25Tiff()))
            .body("_embedded.documents[4].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[5].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[5].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[6].originalDocumentName", Matchers.equalTo(getAttachment27Jpeg()))
            .body("_embedded.documents[6].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_JPEG_VALUE))
            .when()
            .post("/documents");

        String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));
        String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));
        Integer document1Size = response.path("_embedded.documents[0].size");

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", Matchers.equalTo(getAttachment7Png()))
            .body("classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", Matchers.equalTo("caseworker"))
            .body("roles[1]", Matchers.equalTo("citizen"))
            .when()
            .get(documentUrl1);

        assertByteArrayEquality(getAttachment7Png(),
            givenRequest(getCitizen())
                .expect()
                .statusCode(200)
                .contentType(Matchers.equalTo(MediaType.IMAGE_PNG_VALUE))
                .header("OriginalFileName", getAttachment7Png())
                .when()
                .get(documentContentUrl1)
                .asByteArray());
    }

    @Test
    public void cd2AsUnauthenticatedUserIFail403ToUploadFiles() {
        givenUnauthenticatedRequest()
            .multiPart("files", file(getAttachment7Png()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("files", file(getAttachment7Png()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(403)
            .when()
            .post("/documents");
    }

    @Test
    public void cd3AsAuthenticatedUserIFailToUploadAFileWithoutClassification() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd4AsAuthenticatedUserIFailToUploadFilesWithIncorrectClassification() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", "XYZ")
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd5AsAuthenticatedUserIFailedWhenITriedToMakeAPostRequestWithoutAnyFile() {
        givenRequest(getCitizen())
            .multiPart("classification", Classifications.RESTRICTED)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("Provide some files to be uploaded."))
            .when()
            .post("/documents");
    }

    @Test
    public void cd6AsAuthenticatedUserIUploadFileWithNameContainingIllegalCharacters() {
        givenRequest(getCitizen())
            .multiPart("files", file(getIllegalNameFile()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getIllegalNameFile1()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getIllegalNameFile2()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo("uploadFile.jpg"))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[1].originalDocumentName", Matchers.equalTo("uploadFile_-.jpg"))
            .body("_embedded.documents[2].originalDocumentName", Matchers.equalTo("uploadFile9 _-.jpg"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd7AsAuthenticatedUserICanUploadFilesOfDifferentFormat() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200).contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment9Jpg()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[0].roles", Matchers.equalTo(null))
            .body("_embedded.documents[1].originalDocumentName", Matchers.equalTo(getAttachment4Pdf()))
            .body("_embedded.documents[1].mimeType", Matchers.equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[1].classification", Matchers.equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[1].roles", Matchers.equalTo(null))
            .when()
            .post("/documents");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void cd8AsAuthenticatedUserICanNotUploadFilesOfDifferentFormatIfNotOnTheWhitelistExe() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(getBadAttachment1()), MediaType.ALL_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd9AsAuthenticatedUserICannotUploadXmlFile() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment18()), MediaType.APPLICATION_XML_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd10AsAuthenticatedUserICannotUploadSvgFile() {
        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment10()), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd11R1AsAuthenticatedWhenIUploadAFileOnlyFirstTtlWillBeTakenIntoConsideration() {
        if (getToggleTtlEnabled()) {
            givenRequest(getCitizen())
                .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("classification", String.valueOf(Classifications.PUBLIC))
                .multiPart("roles", "citizen").multiPart("roles", "caseworker")
                .multiPart("ttl", "2018-10-31T10:10:10+0000")
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment9Jpg()))
                .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
                .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
                .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
                .body("_embedded.documents[0].ttl", Matchers.equalTo("2018-10-31T10:10:10+0000"))
                .when()
                .post("/documents");
        }

    }

    @Test
    @Pending
    public void cd12R1AsAUserWhenIUploadAFileWithATtlFileWillBeRemovedByBackgroundProcessOnceTtlIsComplete() throws InterruptedException {
        if (getToggleTtlEnabled()) {
            String url = givenRequest(getCitizen())
                .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("classification", String.valueOf(Classifications.PUBLIC))
                .multiPart("roles", "citizen").multiPart("roles", "caseworker")
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment9Jpg()))
                .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
                .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
                .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
                .when()
                .post("/documents")
                .path("_embedded.documents[0]._links.self.href");

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
    public void cd13R1AsAuthenticatedWhenIUploadATiffIGetAnIconInReturn() throws IOException {
        Response response = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment25Tiff()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getAttachment25Tiff()))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .andReturn();

        String tiffUrl = replaceHttp(response.path("_embedded.documents[0]._links.thumbnail.href"));

        byte[] tiffByteArray = givenRequest(getCitizen())
            .get(tiffUrl).asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailNPad.jpg").toPath());

        Assert.assertArrayEquals(tiffByteArray, file);
    }

    @Test
    @Pending
    public void cd14R1AsAuthenticatedUserWhenIUploadAJpegItGetsAThumbnail() throws IOException {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        byte[] downloadedFileByteArray = givenRequest(getCitizen())
            .get(url).asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailJPG.jpg").toPath());

        Assert.assertArrayEquals(downloadedFileByteArray, file);
    }

    @Test
    @Pending
    public void cd15R1AsAuthenticatedUserWhenIUploadAPdfICanGetTheThumbnailOfThatPdf() throws IOException {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getAttachment4Pdf()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        assertByteArrayEquality(getThumbnailPdf(), givenRequest(getCitizen()).get(url).asByteArray());
    }

    @Test
    public void cd16AsAUserIShouldNotBeAbleToUploadAnSvgIfItsRenamedToPdf() {
        givenRequest(getCitizen())
            .multiPart("files", file(getSvgAsPdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd17AsAUserIShouldNotBeAbleToUploadAnXmlIfItsRenamedToPdf() {
        givenRequest(getCitizen())
            .multiPart("files", file(getXmlAsPdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void cd18AsAUserIShouldNotBeAbleToUploadAnExeIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart("files", file(getExeAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd19AsAUserIShouldNotBeAbleToUploadAnSvgIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart("files", file(getSvgAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd20AsAUserIShouldNotBeAbleToUploadAnXmlIfItsRenamedToPng() {
        givenRequest(getCitizen())
            .multiPart("files", file(getXmlAsPng()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd21R1AsAuthenticatedUserIShouldNotBeAbleToUploadGif() {

        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment6Gif()), V1MimeTypes.IMAGE_GIF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd22AsAuthenticatedUserICannotUploadMacroEnabledWord() {
        givenRequest(getCitizen())
            .multiPart("files", file(getWordMacroEnabled()), "application/vnd.ms-word.document.macroEnabled.12")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd23AsAuthenticatedUserICannotUploadMacroEnabledExcel() {
        givenRequest(getCitizen())
            .multiPart("files", file(getExcelTemplateMacroEnabled()), "application/vnd.ms-excel.template.macroEnabled.12")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd24AsAuthenticatedUserICannotUploadMacroEnabledPowerPoint() {
        givenRequest(getCitizen())
            .multiPart("files", file(getPowerPointSlideShowMacroEnabled()), "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void cd25AsAuthenticatedUserICanUploadWithXForwardHeaders() {
        String forwardedHost = "ccd-gateway.service.internal";
        givenRequest(getCitizen())
            .header("x-forwarded-host", forwardedHost)
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0]._links.binary.href", not(containsString(forwardedHost)))
            .when()
            .post("/documents");
    }

    @Test
    public void cd26AsAUserIShouldBeAbleToUploadAFileWithATtl() {

        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2021-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].ttl", equalTo("2021-01-31T10:10:10+0000"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.self.href");
    }

    @Test
    public void cd27AsAuthenticatedUserICannotUploadEncryptedFile() {
        givenRequest(getCitizen())
            .multiPart("files", file(getEncryptedFile()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }
}
