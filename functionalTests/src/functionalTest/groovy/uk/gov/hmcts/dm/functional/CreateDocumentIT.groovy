package uk.gov.hmcts.dm.functional

import groovy.time.TimeCategory
import io.restassured.response.Response
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.dm.functional.utilities.V1MimeTypes

import java.sql.Time
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isOneOf
import static org.junit.Assume.assumeTrue

@RunWith(SpringRunner.class)
class CreateDocumentIT extends BaseIT {


    //as per https://blogs.msdn.microsoft.com/vsofficedeveloper/2008/05/08/office-2007-file-format-mime-types-for-http-content-streaming-2/
    @Test
    void "CD1 (R1) As authenticated user upload 7 files with correct classification and some roles set"() {
        Response response = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_7_PNG), MediaType.IMAGE_PNG_VALUE)
            .multiPart("files", file(ATTACHMENT_8_TIF), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(ATTACHMENT_25_TIFF), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("files", file(ATTACHMENT_27_JPEG), V1MimeTypes.IMAGE_JPEG_VALUE)

            .multiPart("files", file(WORD), "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart("files", file(WORD_TEMPLATE), "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            //.multiPart("files", file(WORD_MACRO_ENABLED), "application/vnd.ms-word.document.macroEnabled.12")
//            .multiPart("files", file(WORD_TEMPLATE_MACRO_ENABLED), "application/vnd.ms-word.template.macroEnabled.12")

            .multiPart("files", file(EXCEL), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//            .multiPart("files", file(EXCEL_MACRO_ENABLED), "application/vnd.ms-excel.sheet.macroEnabled.12")
            .multiPart("files", file(EXCEL_TEMPLATE), "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
//            .multiPart("files", file(EXCEL_TEMPLATE_MACRO_ENABLED), "application/vnd.ms-excel.template.macroEnabled.12")

            .multiPart("files", file(POWER_POINT), "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            //.multiPart("files", file(POWER_POINT_MACRO_ENABLED), "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            .multiPart("files", file(POWER_POINT_TEMPLATE), "application/vnd.openxmlformats-officedocument.presentationml.template")
//            .multiPart("files", file(POWER_POINT_TEMPLATE_MACRO_ENABLED), "application/vnd.ms-powerpoint.template.macroenabled.12")
            .multiPart("files", file(POWER_POINT_SLIDE_SHOW), "application/vnd.openxmlformats-officedocument.presentationml.slideshow")
//            .multiPart("files", file(POWER_POINT_SLIDE_SHOW_MACRO_ENABLED), "application/vnd.ms-powerpoint.slideshow.macroEnabled.12")

            .multiPart("files", file(WORD_OLD), "application/msword")
            .multiPart("files", file(EXCEL_OLD), "application/vnd.ms-excel")
            .multiPart("files", file(POWER_POINT_OLD), "application/vnd.ms-powerpoint")

            .multiPart("files", file(TEXT_ATTACHMENT_1), "text/plain")
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

            .body("_embedded.documents[1].originalDocumentName", equalTo(ATTACHMENT_8_TIF))
            .body("_embedded.documents[1].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[1].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[1].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[1].roles[1]", equalTo("citizen"))

            .body("_embedded.documents[2].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[2].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[2].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[2].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[2].roles[1]", equalTo("citizen"))

            .body("_embedded.documents[3].originalDocumentName", equalTo(ATTACHMENT_4_PDF))
            .body("_embedded.documents[3].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))

            .body("_embedded.documents[4].originalDocumentName", equalTo(ATTACHMENT_25_TIFF))
            .body("_embedded.documents[4].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))

            .body("_embedded.documents[5].originalDocumentName", equalTo(ATTACHMENT_26_BMP))
            .body("_embedded.documents[5].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))

            .body("_embedded.documents[6].originalDocumentName", equalTo(ATTACHMENT_27_JPEG))
            .body("_embedded.documents[6].mimeType", equalTo(V1MimeTypes.IMAGE_JPEG_VALUE))

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
                .header("Content-Length", equalTo(document1Size))
            .when()
                .get(documentContentUrl1)
            .asByteArray()
    }

    @Test
    void "CD2 As unauthenticated user I fail (403) to upload files"() {
        givenUnauthenticatedRequest()
            .multiPart("files", file(ATTACHMENT_7_PNG), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("files", file(ATTACHMENT_7_PNG), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
        .expect()
            .statusCode(403)
        .when()
            .post("/documents")
    }

    @Test
    void "CD3 As authenticated user I fail to upload a file without classification"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
        .expect()
            .statusCode(422)
            .body("error", equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
        .when()
            .post("/documents")
    }

    @Test
    void "CD4 As authenticated user I fail to upload files with incorrect classification"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", "XYZ")
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
        .expect()
            .statusCode(422)
            .body("error", equalTo("Please provide a valid classification: PRIVATE, RESTRICTED or PUBLIC"))
        .when()
            .post("/documents")
    }

    @Test
    void "CD5 As authenticated user I failed when I tried to make a post request without any file"() {
        givenRequest(CITIZEN)
            .multiPart("classification", Classifications.RESTRICTED)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
        .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Provide some files to be uploaded."))
        .when()
            .post("/documents")
    }

    @Test
    void "CD6 As authenticated user I upload file with name containing illegal characters"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ILLEGAL_NAME_FILE), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(ILLEGAL_NAME_FILE1), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(ILLEGAL_NAME_FILE2), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
        .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo("uploadFile.jpg"))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[1].originalDocumentName", equalTo("uploadFile_-.jpg"))
            .body("_embedded.documents[2].originalDocumentName", equalTo("uploadFile9 _-.jpg"))
        .when()
            .post("/documents")
    }


    @Test
    void "CD7 As authenticated user I can upload files of different format"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("files", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
        .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PRIVATE as String))
            .body("_embedded.documents[0].roles", equalTo(null))
            .body("_embedded.documents[1].originalDocumentName", equalTo(ATTACHMENT_4_PDF))
            .body("_embedded.documents[1].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[1].classification", equalTo(Classifications.PRIVATE as String))
            .body("_embedded.documents[1].roles", equalTo(null))
        .when()
            .post("/documents")
    }


    @Test
    void "CD8 As authenticated user I can not upload files of different format if not on the whitelist (.exe)"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("files", file(BAD_ATTACHMENT_1), MediaType.ALL_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
        .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
        .when()
            .post("/documents")
    }

    @Test
    void "CD9 As authenticated user I cannot upload xml file"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_18), MediaType.APPLICATION_XML_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD10 As authenticated user I cannot upload svg file"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_10), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD11 (R1) As authenticated when i upload a file only first TTL will be taken into consideration"() {
        assumeTrue(toggleConfiguration.isTtl())

        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .multiPart("ttl", "2018-01-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].ttl", equalTo("2018-10-31T10:10:10+0000"))
        .when()
        .post("/documents")
    }

    @Test
    void "CD12 (R1) As a user, when i upload a file with a TTL, file will be removed by background process once TTL is complete"() {
        assumeTrue(toggleConfiguration.isTtl())

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        def ttlDate = OffsetDateTime.now().minusMinutes(2)
        def ttlFormatted = ttlDate.format(dtf).toString()
        def url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", ttlFormatted)
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].ttl",
            equalTo(
                ttlDate.atZoneSameInstant(ZoneId.of("GMT+0"))
                .format(dtf))
            )
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.self.href")

        def statusCode = null
        def start = LocalDateTime.now()

        while (statusCode != 404 && (Duration.between(start, LocalDateTime.now()).seconds < 80)) {

            statusCode = givenRequest(CITIZEN)
                .expect()
                .statusCode(isOneOf(404, 200))
                .when()
                .get(url)
                .statusCode()

            sleep(1000)
        }

        givenRequest(CITIZEN)
            .expect()
                .statusCode(404)
            .when()
                .get(url)
    }

    @Test
    void "CD13 (R1) As authenticated when i upload a Tiff I get an icon in return"() {
        def response = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_25_TIFF), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_25_TIFF))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .andReturn()

//        def notepadUrl = response.path("_embedded.documents[0]._links.thumbnail.href")
        def tiffUrl = response.path("_embedded.documents[0]._links.thumbnail.href")

//        def notepadByteArray =  givenRequest(CITIZEN)
//        .get(notepadUrl).asByteArray()

        def tiffByteArray =  givenRequest(CITIZEN)
            .get(tiffUrl).asByteArray()

        def file = file("ThumbnailNPad.jpg").getBytes()

        //Assert.assertTrue(Arrays.equals(notepadByteArray, file))
        Assert.assertTrue(Arrays.equals(tiffByteArray, file))
    }

    @Test
    @Ignore // FIXME RDM-3133 Thumbnail generation timing out
    void "CD14 (R1) As authenticated user when i upload a JPEG, it gets a thumbnail"() {
        def url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href")

        def downloadedFileByteArray =  givenRequest(CITIZEN)
            .get(url).asByteArray()

        def file = file("ThumbnailJPG.jpg").getBytes()
        Assert.assertTrue(Arrays.equals(downloadedFileByteArray, file))
    }

    @Test
    @Ignore("Fail in CNP")
    void "CD15 (R1) As authenticated user when I upload a pdf, I can get the thumbnail of that pdf"() {
        def url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_4_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_4_PDF))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href")

        assertByteArrayEquality THUMBNAIL_PDF, givenRequest(CITIZEN).get(url).asByteArray()
    }

    @Test
    void "CD16 (R1) As authenticated user when I upload a bmp, I can get the thumbnail of that bmp"() {
        def url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href")

        def downloadedFileByteArray =  givenRequest(CITIZEN)
            .get(url).asByteArray()

        def file = file(THUMBNAIL_BMP).getBytes()
        Assert.assertTrue(Arrays.equals(downloadedFileByteArray, file))
    }

    @Test
    void "CD18 As a user I should not be able to upload an exe if its renamed to pdf"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(EXE_AS_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD19 As a user I should not be able to upload an svg if its renamed to pdf"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(SVG_AS_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD20 As a user I should not be able to upload an xml if its renamed to pdf"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(XML_AS_PDF), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }


    @Test
    void "CD21 As a user I should not be able to upload an exe if its renamed to png"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(EXE_AS_PNG), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD22 As a user I should not be able to upload an svg if its renamed to png"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(SVG_AS_PNG), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD23 As a user I should not be able to upload an xml if its renamed to png"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(XML_AS_PNG), MediaType.IMAGE_PNG_VALUE)
            .multiPart("classification", Classifications.PRIVATE as String)
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD24 (R1) As authenticated user I should not be able to upload gif"() {

        givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_6_GIF), V1MimeTypes.IMAGE_GIF_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD25 As authenticated user I cannot upload macro-enabled word"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(WORD_MACRO_ENABLED), "application/vnd.ms-word.document.macroEnabled.12")
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

    @Test
    void "CD26 As authenticated user I cannot upload macro-enabled excel"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(EXCEL_TEMPLATE_MACRO_ENABLED), "application/vnd.ms-excel.template.macroEnabled.12")
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }


    @Test
    void "CD27 As authenticated user I cannot upload macro-enabled power point"() {
        givenRequest(CITIZEN)
            .multiPart("files", file(POWER_POINT_SLIDE_SHOW_MACRO_ENABLED), "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "caseworker")
            .multiPart("roles", "citizen")
            .expect()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .when()
            .post("/documents")
    }

//    @Test
//    void "CD9 As authenticated user I can not upload files that are larger than 100MB"() {
//        givenRequest(CITIZEN)
//                .accept(MediaType.TEXT_HTML_VALUE)
//                .multiPart("files", file(TOO_LARGE_ATTACHMENT), MediaType.TEXT_PLAIN_VALUE)
//                .multiPart("classification", Classifications.PRIVATE as String)
//                .expect().log().all()
//                .statusCode(413)
//                .when()
//                .post("/documents")
//    }
//
//    @Test
//    void "CD10 As authenticated user I can upload files of upto 90MB in size"() {
//        givenRequest(CITIZEN)
//                .accept(MediaType.TEXT_HTML_VALUE)
//                .multiPart("files", file(MAX_SIZE_ALLOWED_ATTACHMENT), MediaType.TEXT_PLAIN_VALUE)
//                .multiPart("classification", Classifications.PRIVATE as String)
//                .expect().log().all()
//                .statusCode(200)
//                .when()
//                .post("/documents")
//    }
}
