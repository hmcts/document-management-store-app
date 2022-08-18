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
    public void CD1__R1__As_authenticated_user_upload_7_files_with_correct_classification_and_some_roles_set() throws IOException {
        Response response = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_7_PNG()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("files", file(getATTACHMENT_8_TIF()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(getATTACHMENT_25_TIFF()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("files", file(getATTACHMENT_27_JPEG()), V1MimeTypes.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getWORD()), "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart("files", file(getWORD_TEMPLATE()), "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            .multiPart("files", file(getEXCEL()), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .multiPart("files", file(getEXCEL_TEMPLATE()), "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
            .multiPart("files", file(getPOWER_POINT()), "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .multiPart("files", file(getPOWER_POINT_TEMPLATE()), "application/vnd.openxmlformats-officedocument.presentationml.template")
            .multiPart("files", file(getPOWER_POINT_SLIDE_SHOW()), "application/vnd.openxmlformats-officedocument.presentationml.slideshow")
            .multiPart("files", file(getWORD_OLD()), "application/msword")
            .multiPart("files", file(getEXCEL_OLD()), "application/vnd.ms-excel")
            .multiPart("files", file(getPOWER_POINT_OLD()), "application/vnd.ms-powerpoint")
            .multiPart("files", file(getTEXT_ATTACHMENT_1()), "text/plain")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen").multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_7_PNG()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_PNG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[1].originalDocumentName", Matchers.equalTo(getATTACHMENT_8_TIF()))
            .body("_embedded.documents[1].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[1].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[1].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[1].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[2].originalDocumentName", Matchers.equalTo(getATTACHMENT_9_JPG()))
            .body("_embedded.documents[2].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[2].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[2].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[2].roles[1]", Matchers.equalTo("citizen"))
            .body("_embedded.documents[3].originalDocumentName", Matchers.equalTo(getATTACHMENT_4_PDF()))
            .body("_embedded.documents[3].mimeType", Matchers.equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[4].originalDocumentName", Matchers.equalTo(getATTACHMENT_25_TIFF()))
            .body("_embedded.documents[4].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[5].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[5].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[6].originalDocumentName", Matchers.equalTo(getATTACHMENT_27_JPEG()))
            .body("_embedded.documents[6].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_JPEG_VALUE))
            .when()
            .post("/documents");

        String documentUrl1 = response.path("_embedded.documents[0]._links.self.href");
        String documentContentUrl1 = response.path("_embedded.documents[0]._links.binary.href");
        Integer document1Size = response.path("_embedded.documents[0].size");

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", Matchers.equalTo(getATTACHMENT_7_PNG()))
            .body("classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("roles[0]", Matchers.equalTo("caseworker"))
            .body("roles[1]", Matchers.equalTo("citizen"))
            .when()
            .get(documentUrl1);

        assertByteArrayEquality(getATTACHMENT_7_PNG(),
            givenRequest(getCITIZEN())
                .expect()
                .statusCode(200)
                .contentType(Matchers.equalTo(MediaType.IMAGE_PNG_VALUE))
                .header("OriginalFileName", getATTACHMENT_7_PNG())
                .when()
                .get(documentContentUrl1)
                .asByteArray());
    }

    @Test
    public void CD2_As_unauthenticated_user_I_fail__403__to_upload_files() {
        givenUnauthenticatedRequest()
            .multiPart("files", file(getATTACHMENT_7_PNG()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("files", file(getATTACHMENT_7_PNG()), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(403)
            .when()
            .post("/documents");
    }

    @Test
    public void CD3_As_authenticated_user_I_fail_to_upload_a_file_without_classification() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD4_As_authenticated_user_I_fail_to_upload_files_with_incorrect_classification() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
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
    public void CD5_As_authenticated_user_I_failed_when_I_tried_to_make_a_post_request_without_any_file() {
        givenRequest(getCITIZEN())
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
    public void CD6_As_authenticated_user_I_upload_file_with_name_containing_illegal_characters() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getILLEGAL_NAME_FILE()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getILLEGAL_NAME_FILE1()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getILLEGAL_NAME_FILE2()), MediaType.IMAGE_JPEG_VALUE)
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
    public void CD7_As_authenticated_user_I_can_upload_files_of_different_format() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200).contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_9_JPG()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[0].roles", Matchers.equalTo(null))
            .body("_embedded.documents[1].originalDocumentName", Matchers.equalTo(getATTACHMENT_4_PDF()))
            .body("_embedded.documents[1].mimeType", Matchers.equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[1].classification", Matchers.equalTo(String.valueOf(Classifications.PRIVATE)))
            .body("_embedded.documents[1].roles", Matchers.equalTo(null))
            .when()
            .post("/documents");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void CD8_As_authenticated_user_I_can_not_upload_files_of_different_format_if_not_on_the_whitelist__exe_() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(getBAD_ATTACHMENT_1()), MediaType.ALL_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", Matchers.equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD9_As_authenticated_user_I_cannot_upload_xml_file() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_18()), MediaType.APPLICATION_XML_VALUE)
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
    public void CD10_As_authenticated_user_I_cannot_upload_svg_file() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_10()), V1MimeTypes.IMAGE_SVG_VALUE)
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
    public void CD11__R1__As_authenticated_when_i_upload_a_file_only_first_TTL_will_be_taken_into_consideration() {
        if (getToggleTtlEnabled()) {
            givenRequest(getCITIZEN())
                .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("classification", String.valueOf(Classifications.PUBLIC))
                .multiPart("roles", "citizen").multiPart("roles", "caseworker")
                .multiPart("ttl", "2018-10-31T10:10:10+0000")
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_9_JPG()))
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
    public void CD12__R1__As_a_user__when_i_upload_a_file_with_a_TTL__file_will_be_removed_by_background_process_once_TTL_is_complete() throws InterruptedException {
        if (getToggleTtlEnabled()) {
            String url = givenRequest(getCITIZEN())
                .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("classification", String.valueOf(Classifications.PUBLIC))
                .multiPart("roles", "citizen").multiPart("roles", "caseworker")
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(200)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
                .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_9_JPG()))
                .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
                .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
                .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
                .when()
                .post("/documents")
                .path("_embedded.documents[0]._links.self.href");

            int statusCode = -1;
            LocalDateTime start = LocalDateTime.now();

            while (statusCode != 404 && (Duration.between(start, LocalDateTime.now()).getSeconds() < 600)) {

                statusCode = givenRequest(getCITIZEN())
                    .expect()
                    .statusCode(Matchers.is(oneOf(404, 200)))
                    .when()
                    .get(url)
                    .statusCode();
                Thread.sleep(1000);
            }
            givenRequest(getCITIZEN())
                .expect()
                .statusCode(404)
                .when()
                .get(url);
        }

    }

    @Test
    public void CD13__R1__As_authenticated_when_i_upload_a_Tiff_I_get_an_icon_in_return() throws IOException {
        Response response = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_25_TIFF()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_25_TIFF()))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .andReturn();

        String tiffUrl = response.path("_embedded.documents[0]._links.thumbnail.href");

        byte[] tiffByteArray = givenRequest(getCITIZEN())
            .get(tiffUrl).asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailNPad.jpg").toPath());

        Assert.assertArrayEquals(tiffByteArray, file);
    }

    @Test
    @Pending
    public void CD14__R1__As_authenticated_user_when_i_upload_a_JPEG__it_gets_a_thumbnail() throws IOException {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        byte[] downloadedFileByteArray = givenRequest(getCITIZEN())
            .get(url).asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailJPG.jpg").toPath());

        Assert.assertArrayEquals(downloadedFileByteArray, file);
    }

    @Test
    @Pending
    public void CD15__R1__As_authenticated_user_when_I_upload_a_pdf__I_can_get_the_thumbnail_of_that_pdf() throws IOException {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_4_PDF()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        assertByteArrayEquality(getTHUMBNAIL_PDF(), givenRequest(getCITIZEN()).get(url).asByteArray());
    }

    @Test
    public void CD16_As_a_user_I_should_not_be_able_to_upload_an_svg_if_its_renamed_to_pdf() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getSVG_AS_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD17_As_a_user_I_should_not_be_able_to_upload_an_xml_if_its_renamed_to_pdf() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getXML_AS_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void CD18_As_a_user_I_should_not_be_able_to_upload_an_exe_if_its_renamed_to_png() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getEXE_AS_PNG()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD19_As_a_user_I_should_not_be_able_to_upload_an_svg_if_its_renamed_to_png() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getSVG_AS_PNG()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD20_As_a_user_I_should_not_be_able_to_upload_an_xml_if_its_renamed_to_png() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getXML_AS_PNG()), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD21__R1__As_authenticated_user_I_should_not_be_able_to_upload_gif() {

        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_6_GIF()), V1MimeTypes.IMAGE_GIF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents");
    }

    @Test
    public void CD22_As_authenticated_user_I_cannot_upload_macro_enabled_word() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getWORD_MACRO_ENABLED()), "application/vnd.ms-word.document.macroEnabled.12")
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
    public void CD23_As_authenticated_user_I_cannot_upload_macro_enabled_excel() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getEXCEL_TEMPLATE_MACRO_ENABLED()), "application/vnd.ms-excel.template.macroEnabled.12")
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
    public void CD24_As_authenticated_user_I_cannot_upload_macro_enabled_power_point() {
        givenRequest(getCITIZEN())
            .multiPart("files", file(getPOWER_POINT_SLIDE_SHOW_MACRO_ENABLED()), "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
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
    public void CD25_As_authenticated_user_I_can_upload_with_x_forward_headers() {
        String forwardedHost = "ccd-gateway.service.internal";
        givenRequest(getCITIZEN())
            .header("x-forwarded-host", forwardedHost)
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PRIVATE))
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0]._links.binary.href", not(containsString(forwardedHost)))
            .when()
            .post("/documents");
    }

    @Test
    public void CD26_As_a_user__I_should_be_able_to_upload_a_file_with_a_TTL() {

        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2021-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].ttl", equalTo("2021-01-31T10:10:10+0000"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.self.href");
    }
}
