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
    public void ACV1_As_authenticated_user_who_is_an_owner_POST_a_new_version_of_the_content_to_an_existing_document_then_expect_201() {

        String documentURL = createDocumentAndGetUrlAs(getCITIZEN());

        Response response = givenRequest(getCITIZEN())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentURL)
            .thenReturn();


        String newVersionUrl = response.getHeader("Location");

        givenRequest(getCITIZEN()).expect().statusCode(200).when().get(newVersionUrl);

    }

    @Test
    public void ACV2_As_authenticated_user_POST_a_new_version_of_the_content_to_a_not_existing_document() {

        givenRequest(getCITIZEN())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post("/documents" + UUID.randomUUID());

    }

    @Test
    public void ACV3_As_unauthenticated_user_POST_a_new_version_of_the_content_to_a_not_existing_document() {

        givenUnauthenticatedRequest()
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post("/documents/" + UUID.randomUUID());

    }

    @Test
    public void ACV4_As_unauthenticated_user_POST_a_new_version_of_the_content_to_an_existing_document() {

        String url = createDocumentAndGetUrlAs(getCITIZEN());

        givenUnauthenticatedRequest()
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url);
    }

    @Test
    public void ACV5_As_authenticated_user_who_is_an_not_an_owner__POST_a_new_version_of_the_content_to_an_existing_document() {

        String url = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN_2())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url);
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing this test to fail in CI")
    public void ACV6_As_authenticated_user_who_is_not_an_owner_and_is_a_case_worker() {

        String url = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCASE_WORKER())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .when()
            .post(url);
    }

    @Test
    public void ACV7_As_authenticated_user_who_is_a_case_worker_POST_a_new_version_of_the_content_to_a_not_existing_document_and_expect_404() {

        givenRequest(getCASE_WORKER())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(404)
            .when()
            .post("documents/" + UUID.randomUUID());

    }

    @Test
    public void ACV8_As_an_authenticated_user_and_the_owner_I_should_not_be_able_to_upload_multiple_new_content_versions_then_expect_201() {

        String documentURL = createDocumentAndGetUrlAs(getCITIZEN());
        Response response = givenRequest(getCITIZEN())
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("file", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .multiPart("file", file(getATTACHMENT_3()), MediaType.TEXT_PLAIN_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .post(documentURL)
            .thenReturn();

        String newVersionUrl = response.getHeader("Location");

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl);
    }

    @Test
    public void ACV9_As_an_authenticated_user_and_the_owner_I_should_be_able_to_upload_new_version_of_different_format() {

        String documentURL = createDocumentAndGetUrlAs(getCITIZEN());
        Response response = givenRequest(getCITIZEN())
            .multiPart("file", file(getATTACHMENT_4_PDF()), MediaType.APPLICATION_PDF_VALUE)
            .expect().log().all()
            .statusCode(201)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getATTACHMENT_4_PDF()))
            .body("mimeType", equalTo(MediaType.APPLICATION_PDF_VALUE))
            .when()
            .post(documentURL)
            .thenReturn();

        String newVersionUrl = response.getHeader("Location");

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .when()
            .get(newVersionUrl);

    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void ACV10_As_an_authenticated_user_and_the_owner_I_should_not_be_able_to_upload_exes() {

        String documentURL = createDocumentAndGetUrlAs(getCITIZEN());
        givenRequest(getCITIZEN())
            .multiPart("file", file(getBAD_ATTACHMENT_1()), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentURL);
    }

    @Test
    public void ACV11_As_an_authenticated_user_and_the_owner_I_should_not_be_able_to_upload_zip() {

        String documentURL = createDocumentAndGetUrlAs(getCITIZEN());
        givenRequest(getCITIZEN())
            .multiPart("file", file(getBAD_ATTACHMENT_2()), MediaType.ALL_VALUE)
            .expect().log().all()
            .statusCode(422)
            .when()
            .post(documentURL);
    }

    @Test
    public void ACV12_As_an_owner_I_cannot_update_the_TTL_while_adding_a_version_to_the_document() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTTL(getCASE_WORKER());

            String documentUrl1 = response.path("_embedded.documents[0]._links.self.href");

            givenRequest(getCASE_WORKER())
                .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(201)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
                .body("originalDocumentName", Matchers.equalTo(getATTACHMENT_9_JPG()))
                .body("mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
                .when()
                .post(documentUrl1);

            givenRequest(getCASE_WORKER())
                .expect().log().all()
                .statusCode(200)
                .body("ttl", Matchers.equalTo("2018-10-31T10:10:10+0000"))
                .when()
                .get(documentUrl1);
        }

    }
}
