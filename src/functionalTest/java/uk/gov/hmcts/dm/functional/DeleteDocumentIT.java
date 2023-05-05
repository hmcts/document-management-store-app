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
        this.citizenDocumentUrl = createDocumentAndGetUrlAs(getCitizen());
        this.caseWorkerDocumentUrl = createDocumentAndGetUrlAs(getCaseWorker());
    }

    @Test
    public void d1UnauthenticatedUserCannotDelete() {
        givenUnauthenticatedRequest()
            .expect()
            .statusCode(401)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void d2AuthenticatedUserCanDeleteTheirOwnDocuments() {
        givenRequest(getCitizen())
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl);

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);
    }

    @Test
    public void d3AuthenticatedUserCannotDeleteOtherUserSDocuments() {
        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void d4CaseWorkerCannotDeleteOtherUsersDocuments() {
        givenRequest(getCaseWorker())
            .expect()
            .statusCode(403)
            .when()
            .delete(citizenDocumentUrl);
    }

    @Test
    public void d5CaseWorkerCanDeleteTheirOwnDocument() {
        givenRequest(getCaseWorker())
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl);

        givenRequest(getCaseWorker())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);
    }

    @Test
    public void d6CaseWorkerCanHardDeleteTheirOwnDocument() {
        final Response metadata = fetchDocumentMetaDataAs(getCaseWorker(), caseWorkerDocumentUrl);

        String doc = metadata.body().jsonPath().get("_embedded.allDocumentVersions._embedded.documentVersions[0]._links.self.href");
        String[] split = doc
            .split("/");
        String versionId = split[split.length - 1];
        Assert.assertTrue("Document with version " + versionId + " should exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCaseWorker()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));

        givenRequest(getCaseWorker())
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl + "?permanent=true");


        givenRequest(getCaseWorker())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);

        Assert.assertFalse("Document with version " + versionId + " should NOT exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCaseWorker()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));
    }

    @Test
    public void d7UserCanHardDeleteTheirOwnDocument() {

        final Response metadata = fetchDocumentMetaDataAs(getCitizen(), citizenDocumentUrl);

        String doc = metadata.body().jsonPath().get("_embedded.allDocumentVersions._embedded.documentVersions[0]._links.self.href");
        String[] split = doc
            .split("/");
        String versionId = split[split.length - 1];

        Assert.assertTrue("Document with version " + versionId + " should exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCitizen()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));

        givenRequest(getCitizen())
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl + "?permanent=true");

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);

        Assert.assertFalse("Document with version " + versionId + " should NOT exist (" + metadata.body().print() + ")",
            Boolean.parseBoolean(givenRequest(getCitizen()).when().get("/testing/azure-storage-binary-exists/" + versionId).print()));
    }

    @Test
    public void d8AsAnOwnerOfTheDocumentICouldNotGetTheTtlInfoOnceIHaveDoneTheSoftDelete() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTtl(getCaseWorker());

            String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));
            String documentContentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.binary.href"));

            givenRequest(getCaseWorker())
                .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all().statusCode(201)
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

            givenRequest(getCaseWorker())
                .expect().log().all()
                .statusCode(204)
                .body(CoreMatchers.not(CoreMatchers.containsString("ttl:")))
                .when()
                .delete(documentUrl1);

        }

    }

    @Test
    public void d9AsAnOwnerOfTheDocumentICouldNotGetTheTtlInfoOnceIHaveDoneTheHardDelete() {
        if (getToggleTtlEnabled()) {

            Response response = createAUserForTtl(getCaseWorker());

            String documentUrl1 = replaceHttp(response.path("_embedded.documents[0]._links.self.href"));

            givenRequest(getCaseWorker())
                .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
                .multiPart("ttl", "2018-01-31T10:10:10+0000")
                .expect().log().all()
                .statusCode(201)
                .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
                .body("originalDocumentName", equalTo(getAttachment9Jpg()))
                .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
                .when()
                .post(documentUrl1);

            givenRequest(getCaseWorker())
                .expect().log().all()
                .statusCode(200)
                .body("ttl", equalTo("2018-10-31T10:10:10+0000"))
                .when()
                .get(documentUrl1);

            givenRequest(getCaseWorker())
                .expect().log().all()
                .statusCode(204)
                .body(not(containsString("ttl:")))
                .when()
                .delete(documentUrl1 + "?permanent=true");
        }

    }

    @Test
    public void d10CcdCaseDisposerCanDeleteDocumentCreatedByCaseworker() {
        givenCcdCaseDisposerRequest()
            .expect()
            .statusCode(204)
            .when()
            .delete(caseWorkerDocumentUrl);

        givenRequest(getCaseWorker())
            .expect()
            .statusCode(404)
            .when()
            .get(caseWorkerDocumentUrl);
    }

    @Test
    public void d11CcdCaseDisposerCanDeleteDocumentCreatedByCitizen() {
        givenCcdCaseDisposerRequest()
            .expect()
            .statusCode(204)
            .when()
            .delete(citizenDocumentUrl);

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get(citizenDocumentUrl);
    }
}
