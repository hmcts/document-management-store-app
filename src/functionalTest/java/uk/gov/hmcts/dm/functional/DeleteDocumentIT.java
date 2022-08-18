package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;

public class DeleteDocumentIT extends BaseIT {
    private String citizenDocumentUrl;

    private String caseWorkerDocumentUrl;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Before
    public void setup() {
        this.citizenDocumentUrl = createDocumentAndGetUrlAs(getCITIZEN());
        this.caseWorkerDocumentUrl = createDocumentAndGetUrlAs(getCASE_WORKER());
    }

    @Test
    public void D1_Unauthenticated_user_cannot_delete() {
        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void D2_Authenticated_user_can_delete_their_own_documents() {
        givenRequest(getCITIZEN())
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl);

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);
    }

    @Test
    public void D3_Authenticated_user_cannot_delete_other_user_s_documents() {
        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void D4_Case_worker_cannot_delete_other_users__documents() {
        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void D5_Case_worker_can_delete_their_own_document() {
        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl);

        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);
    }

    @Test
    public void D6_Case_worker_can_hard_delete_their_own_document() {
        final Response metadata = fetchDocumentMetaDataAs(getCASE_WORKER(), caseWorkerDocumentUrl);

        String doc = metadata.body().jsonPath().get("_embedded.allDocumentVersions._embedded.documentVersions[0]._links.self.href");
        String[] split = doc
            .split("/");
        String versionId = split[split.length - 1];
        Assert.assertTrue("Document with version " + versionId + " should exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCASE_WORKER()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));

        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl + "?permanent=true");


        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);

        Assert.assertFalse("Document with version " + versionId + " should NOT exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCASE_WORKER()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));
    }

    @Test
    public void D7_User_can_hard_delete_their_own_document() {

        final Response metadata = fetchDocumentMetaDataAs(getCITIZEN(), citizenDocumentUrl);

        String doc = metadata.body().jsonPath().get("_embedded.allDocumentVersions._embedded.documentVersions[0]._links.self.href");
        String[] split = doc
            .split("/");
        String versionId = split[split.length - 1];

        Assert.assertTrue("Document with version " + versionId + " should exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCITIZEN()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl + "?permanent=true");

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);

        Assert.assertFalse("Document with version " + versionId + " should NOT exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCITIZEN()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));
    }

    @Test
    public void D8_As_an_owner_of_the_document_I_could_not_get_the_TTL_info_once__I_have_done_the_soft_delete() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTTL(getCASE_WORKER());

            String documentUrl1 = response.path("_embedded.documents[0]._links.self.href");
            String documentContentUrl1 = response.path("_embedded.documents[0]._links.binary.href");

            givenRequest(getCASE_WORKER())
                .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all().statusCode(201)
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

            givenRequest(getCASE_WORKER())
                .expect().log().all()
                .statusCode(204)
                .body(CoreMatchers.not(CoreMatchers.containsString("ttl:")))
                .when()
                .delete(documentUrl1);

        }

    }

    @Test
    public void D9_As_an_owner_of_the_document_I_could_not_get_the_TTL_info_once__I_have_done_the_hard_delete() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTTL(getCASE_WORKER());

            String documentUrl1 = response.path("_embedded.documents[0]._links.self.href");

            givenRequest(getCASE_WORKER())
                .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(201)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
                .body("originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
                .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
                .when()
                .post(documentUrl1);

            givenRequest(getCASE_WORKER())
                .expect().log().all()
                .statusCode(200)
                .body("ttl", equalTo("2018-10-31T10:10:10+0000"))
                .when()
                .get(documentUrl1);

            givenRequest(getCASE_WORKER())
                .expect().log().all()
                .statusCode(204)
                .body(not(containsString("ttl:")))
                .when()
                .delete(documentUrl1 + "?permanent=true");
        }

    }

    @Test
    public void D10_Ccd_Case_Disposer_can_delete_document_created_by_caseworker() {
        givenCcdCaseDisposerRequest()
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl);

        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);
    }

    @Test
    public void D11_Ccd_Case_Disposer_can_delete_document_created_by_citizen() {
        givenCcdCaseDisposerRequest()
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl);

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);
    }
}
