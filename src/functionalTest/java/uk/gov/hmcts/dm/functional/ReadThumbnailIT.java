package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ReadThumbnailIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void rt1AsAnAuthenticatedWhenIUploadATiffIGetAnIconInReturn() throws IOException {
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
            .get(tiffUrl)
            .asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailNPad.jpg").toPath());

        Assert.assertArrayEquals(tiffByteArray, file);
    }

    @Test
    public void rt2AsAnAuthenticatedUserWhenIUploadABmpICanGetTheThumbnailOfThatBmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        Assert.assertNotNull(givenRequest(getCitizen())
            .get(url)
            .asByteArray());
    }

    @Test
    public void rt3AsAnAuthenticatedUserWhenIUploadABmpICanGetTheVersionOfThumbnailOfThatBmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        Assert.assertNotNull(givenRequest(getCitizen()).get(url).asByteArray());
    }

    @Test
    public void rt4AsAnUnauthenticatedUserICanNotGetTheVersionOfThumbnailOfThatBmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen").expect().log().all().statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        url = replaceHttp(url);

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", Matchers.equalTo("Access Denied"))
            .log().all();
    }

    @Test
    public void rt5AsUnauthenticatedUserICanNotGetTheThumbnailOfABmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        url = replaceHttp(url);

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", Matchers.equalTo("Access Denied"))
            .log().all();
    }

    @Test
    public void rt6AsAnAuthenticatedUserICanNotFindTheThumbnailOfNonExistentBmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        String documentStr = "documents/";

        String documentId = url.substring(url.indexOf(documentStr) + documentStr.length(), url.lastIndexOf("/"));

        String nonExistentId = UUID.randomUUID().toString();

        String nonExistentIdUrl = url.replace(documentId, nonExistentId);

        givenRequest(getCitizen())
            .when()
            .get(nonExistentIdUrl)
            .then()
            .assertThat()
            .statusCode(404)
            .log().all();
    }

    @Test
    public void rt7AsAnAuthenticatedUserICanNotFindTheVersionOfThumbnailOfNonExistentBmp() {
        String url = givenRequest(getCitizen())
            .multiPart("files", file(getAttachment26Bmp()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getAttachment26Bmp()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        String versionsStr = "versions/";
        String versionId = url.substring(url.indexOf(versionsStr) + versionsStr.length(), url.lastIndexOf("/"));
        String nonExistentVersionId = UUID.randomUUID().toString();
        String nonExistentVersionIdUrl = url.replace(versionId, nonExistentVersionId);

        givenRequest(getCitizen())
            .when()
            .get(nonExistentVersionIdUrl)
            .then()
            .assertThat()
            .statusCode(404)
            .log().all();
    }
}
