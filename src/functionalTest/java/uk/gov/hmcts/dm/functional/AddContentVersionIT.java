package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class AddContentVersionIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void acv1AsAuthenticatedUserWhoIsAnOwnerPostANewVersionOfTheContentToAnExistingDocumentThenExpect201() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        Response response = givenRequest(getCitizen())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentUrl)
            .thenReturn();


        String newVersionUrl = response.getHeader("Location");

        newVersionUrl = replaceHttp(newVersionUrl);

        givenRequest(getCitizen()).expect().statusCode(200).when().get(newVersionUrl);

    }

    @Test
    public void acv2AsAuthenticatedUserPostANewVersionOfTheContentToANotExistingDocument() {

        givenRequest(getCitizen())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post("/documents" + UUID.randomUUID());

    }

    @Test
    public void acv3AsUnauthenticatedUserPostANewVersionOfTheContentToANotExistingDocument() {

        givenUnauthenticatedRequest()
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(401)
            .when()
            .post("/documents/" + UUID.randomUUID());

    }

    @Test
    public void acv4AsUnauthenticatedUserPostANewVersionOfTheContentToAnExistingDocument() {

        String url = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(401)
            .when()
            .post(url);
    }

    @Test
    public void acv5AsAuthenticatedUserWhoIsAnNotAnOwnerPostANewVersionOfTheContentToAnExistingDocument() {

        String url = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen2())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url);
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing this test to fail in CI")
    public void acv6AsAuthenticatedUserWhoIsNotAnOwnerAndIsACaseWorker() {

        String url = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCaseWorker())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url);
    }

    @Test
    public void acv7AsAuthenticatedUserWhoIsACaseWorkerPostANewVersionOfTheContentToANotExistingDocumentAndExpect404() {

        givenRequest(getCaseWorker())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post("documents/" + UUID.randomUUID());

    }

    @Test
    public void acv8AsAnAuthenticatedUserAndTheOwnerIShouldNotBeAbleToUploadMultipleNewContentVersionsThenExpect201() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        Response response = givenRequest(getCitizen())
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("file", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("file", file(getAttachment3()), MediaType.TEXT_PLAIN_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentUrl)
            .thenReturn();

        String newVersionUrl = response.getHeader("Location");
        newVersionUrl = replaceHttp(newVersionUrl);

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl);
    }

    @Test
    public void acv9AsAnAuthenticatedUserAndTheOwnerIShouldBeAbleToUploadNewVersionOfDifferentFormat() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        Response response = givenRequest(getCitizen())
            .multiPart("file", file(getAttachment4Pdf()), MediaType.APPLICATION_PDF_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getAttachment4Pdf()))
            .body("mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .when()
            .post(documentUrl)
            .thenReturn();

        String newVersionUrl = response.getHeader("Location");
        newVersionUrl = replaceHttp(newVersionUrl);

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl);

    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void acv10AsAnAuthenticatedUserAndTheOwnerIShouldNotBeAbleToUploadExes() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        givenRequest(getCitizen())
            .multiPart("file", file(getBadAttachment1()), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentUrl);
    }

    @Test
    public void acv11AsAnAuthenticatedUserAndTheOwnerIShouldNotBeAbleToUploadZip() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        givenRequest(getCitizen())
            .multiPart("file", file(getBadAttachment2()), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentUrl);
    }

    @Test
    public void acv12AsAnOwnerICannotUpdateTheTtlWhileAddingAVersionToTheDocument() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTtl(getCaseWorker());

            String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));

            givenRequest(getCaseWorker())
                .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(201)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
                .body("originalDocumentName", Matchers.equalTo(getAttachment9Jpg()))
                .body("mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
                .when()
                .post(documentUrl1);

            givenRequest(getCaseWorker())
                .expect().log().all()
                .statusCode(200)
                .body("ttl", Matchers.equalTo("2018-10-31T10:10:10+0000"))
                .when()
                .get(documentUrl1);
        }

    }
}
