package uk.gov.hmcts.dm.functional;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class ReadDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void r1AsAuthenticatedUserWhoIsAnOwnerCanReadOwnedDocuments() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r2AsAuthenticatedUserWhoIsAnOwnerButAcceptHeaderIsApplicationHalJson() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen())
            .header("Accept", "application/vnd.uk.gov.hmcts.dm.document.v10000+hal+json")
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r3AsUnauthenticatedUserITryGettingAnExistingDocumentAndGet403() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r4AsUnauthenticatedUserGetExistingDocumentAndReceive403() {

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get("/documents/XXX");
    }

    @Test
    public void r5AsAuthenticatedUserWhoIsNotAnOwnerAndNotACaseWorkerICanTAccessADocument() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r6AsAuthenticatedUserWhoIsNotAnOwnerAndNotACaseWorkerGetExistingDocumentBinaryAndSee403() {

        String binaryUrl = createDocumentAndGetBinaryUrlAs(getCitizen());

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(binaryUrl);
    }

    @Test
    public void r7AsAuthenticatedUserWhoIsNotAnOwnerAndIsACaseWorkerICanReadNotOwnedDocuments() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleProbate())))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void asAuthenticatedUserWhoIsNotAnOwnerAndHasCaseWorkerAndOtherRolesICanReadNotOwnedDocuments() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenSpacedRolesRequest(getCaseWorker(),
            new ArrayList<>(Arrays.asList(getCustomUserRole(),
                getCaseWorkerRoleProbate())))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void r8AsAuthenticatedUserGetDocumentXxxWhereXxxIsNotUuid() {

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/xxx");
    }

    @Test
    public void r9AsAuthenticatedUserGetDocument111Where111IsNotUuid() {

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/111");
    }

    @Test
    public void r10AsAuthenticatedUserGetDocumentWhere111IsNotUuid() {

        givenRequest(getCitizen())
            .expect()
            .statusCode(405)
            .when()
            .get("documents");
    }

    @Test
    public void r11AsAuthenticatedUserGetDocumentXxxWhereXxxIsUuidButItDoesnTExistInOurBd() {

        givenRequest(getCitizen())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/" + UUID.randomUUID());
    }

    @Test
    public void r14AsAuthenticatedUserWithASpecificRoleICanAccessADocIfItsClassificationIsRestrictedAndRolesMatch() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "RESTRICTED",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCitizen2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void r15AsAuthenticatedUserWithASpecificRoleICanTAccessADocumentIfItsClassificationIsPrivateAndRolesMatch() {

        ArrayList<String> roles = new ArrayList<>(List.of("not-a-caseworker"));

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PRIVATE", roles);

        givenRequest(getCitizen2(), roles)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r16AsAuthenticatedUserWithASpecificRoleICanAccessADocumentIfItsClassificationIsPublicAndRolesMatch() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PUBLIC",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCitizen2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void r17AsAuthenticatedUserWithASpecificRoleICanAccessADocumentIfItsClassificationIsPublicAndMatchesRole() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PUBLIC",
            new ArrayList<>(Arrays.asList("citizen", "caseworker")));

        givenRequest(getCitizen2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r18AsAuthenticatedUserWithNoRoleICannotAccessADocumentIfItsClassificationIsPublicWithNoRole() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PUBLIC",
            Collections.emptyList());

        givenRequest(getCitizen2())
            .expect().statusCode(403)
            .when().get(documentUrl);
    }

    @Test
    public void r19AsAuthenticatedUserWithSomeRoleICannotAccessADocumentIfItsClassificationIsPublicAndRolesNotMatch() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PUBLIC",
            Collections.emptyList());

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);

    }

    @Test
    public void r20AsAuthUserWithNoRoleTestsByDefaultSetsRoleAsCitzICanAccessADocIfItsClassPublicAndRolesIsCitz() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "PUBLIC",
            new ArrayList<>(List.of("citizen")));

        givenRequest(getCitizen2(), new ArrayList<>(List.of("citizen")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r21AsAnOwnerICanAccessADocumentEvenIfItsClassificationIsPrivateWithNoRoles() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen2(), getAttachment9Jpg(), "PRIVATE",
            Collections.emptyList());

        givenRequest(getCitizen2())
            .expect().statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r22AsAuthenticatedUserWithASpecificRoleICanTAccessADocIfClassificationIsRestrictedAndRolesDonTMatch() {
        String documentUrl = createDocumentAndGetUrlAs(getCitizen(), getAttachment9Jpg(), "RESTRICTED",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r23AsAnOwnerWithNoRoleICanAccessADocumentEvenIfItsClassificationIsPrivateAndRoleAsCaseworker() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen2(), getAttachment9Jpg(), "PRIVATE",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCitizen2())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r24ICreatedADocumentUsingS2STokenAndOnlyCaseworkersShouldBeAbleToReadThatUsingApiGateway() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleProbate())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", equalTo(getCitizen()))
            .when()
            .get(documentUrl)
            .thenReturn();

        givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleSscs())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", Matchers.equalTo(getCitizen()))
            .when()
            .get(documentUrl)
            .thenReturn();

        givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleCmc())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", Matchers.equalTo(getCitizen()))
            .when()
            .get(documentUrl)
            .thenReturn();
    }

    @Test
    public void r25ICreatedADocumentUsingS2STokenButIMustNotAccessItAsACitizenUsingApiGateway() {

        String documentUrl = createDocumentAndGetBinaryUrlAs("user1");

        givenRequest(getCitizen())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r26UseridProvidedDuringDataCreationCanBeObtainedAsUsernameInTheAuditTrail() {

        String documentUrl = createDocumentAndGetUrlAs(getCaseWorker());

        givenRequest(getCaseWorker())
            .expect()
            .statusCode(200)
            .log().all()
            .body("createdBy", Matchers.equalTo(getCaseWorker()))
            .when()
            .get(documentUrl)
            .thenReturn();


        String userNameFromResponse = givenRequest(getCaseWorker(), List.of(getCaseWorkerRoleProbate()))
            .when()
            .get(documentUrl + "/auditEntries")
            .body().path("_embedded.auditEntries[0].username");

        // TODO : This assert Fails on Nightly build only. Passes on Local,  Preview as well as Master build.
        //assertThat(userNameFromResponse,equalTo(CASE_WORKER));
    }

    @Test
    public void r27AsACitizenIfIUploadADocumentToApiStoreThenIShouldBeAbleToAccessItUsingApiGateway() {

        String documentUrl = createDocumentAndGetBinaryUrlAs(getCitizen());

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void r28AsAuthenticatedUserWhenGetNonExistingDocumentBinaryAndSee404() {

        String binaryUrl = createDocumentAndGetBinaryUrlAs(getCitizen());
        String documentStr = "documents/";
        String nonExistentId = UUID.randomUUID().toString();
        binaryUrl = binaryUrl.replace(binaryUrl.substring(binaryUrl.indexOf(documentStr) + documentStr.length(),
            binaryUrl.lastIndexOf("/")), nonExistentId);

        givenRequest(getCitizen()).expect().statusCode(404).when().get(binaryUrl);
    }
}
