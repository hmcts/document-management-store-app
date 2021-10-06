package uk.gov.hmcts.dm.functional

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Disabled
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.dm.functional.utilities.V1MimeTypes
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

@Disabled
class ReadThumbnailIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "RT1  As an authenticated when i upload a Tiff I get an icon in return"() {
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

        def tiffUrl = response.path("_embedded.documents[0]._links.thumbnail.href")

        def tiffByteArray = givenRequest(CITIZEN)
            .get(tiffUrl).asByteArray()

        def file = file("ThumbnailNPad.jpg").getBytes()

        Assert.assertTrue(Arrays.equals(tiffByteArray, file))
    }

    @Test
    void "RT2  As an authenticated user when I upload a bmp, I can get the thumbnail of that bmp"() {
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

        Assert.assertNotNull givenRequest(CITIZEN).get(url).asByteArray()
    }

    @Test
    void "RT3  As an authenticated user when I upload a bmp, I can get the version of thumbnail of that bmp"() {
        String url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP as Object))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href")

        Assert.assertNotNull givenRequest(CITIZEN).get(url).asByteArray()
    }

    @Test
    void "RT4  As an unauthenticated user I can not get the version of thumbnail of that bmp"() {
        String url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP as Object))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href")

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", equalTo("Access Denied"))
            .log()
            .all();
    }

    @Test
    void "RT5  As unauthenticated user I can not get the thumbnail of a bmp"() {
        String url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP as Object))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href")

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", equalTo("Access Denied"))
            .log()
            .all();
    }

    @Test
    void "RT6  As an authenticated user, I can not find the thumbnail of non existent bmp"() {
        String url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP as Object))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href")

        def documentStr = "documents/"

        def documentId = url.substring(url.indexOf(documentStr) + documentStr.length(), url.lastIndexOf("/"))

        def nonExistentId = UUID.randomUUID().toString()

        String nonExistentIdURL = url.replace(documentId, nonExistentId);

        givenRequest(CITIZEN)
            .when()
            .get(nonExistentIdURL)
            .then()
            .assertThat()
            .statusCode(404)
            .log()
            .all();
    }

    @Test
    void "RT7  As an authenticated user, I can not find the version of thumbnail of non existent bmp"() {
        String url = givenRequest(CITIZEN)
            .multiPart("files", file(ATTACHMENT_26_BMP), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_26_BMP as Object))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href")

        def versionsStr = "versions/"
        def versionId = url.substring(url.indexOf(versionsStr) + versionsStr.length(), url.lastIndexOf("/"))
        def nonExistentVersionId = UUID.randomUUID().toString()
        String nonExistentVersionIdURL = url.replace(versionId, nonExistentVersionId);

        givenRequest(CITIZEN)
            .when()
            .get(nonExistentVersionIdURL)
            .then()
            .assertThat()
            .statusCode(404)
            .log()
            .all();
    }
}
