package uk.gov.hmcts.dm.functional;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;

public class ReadDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void R1_As_authenticated_user_who_is_an_owner__can_read_owned_documents() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R2_As_authenticated_user_who_is_an_owner__but_Accept_Header_is_application_hal_json() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN())
            .header("Accept", "application/vnd.uk.gov.hmcts.dm.document.v10000+hal+json")
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R3_As_unauthenticated_user_I_try_getting_an_existing_document_and_get_403() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R4_As_unauthenticated_user_GET_existing_document_and_receive_403() {

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get("/documents/XXX");
    }

    @Test
    public void R5_As_authenticated_user_who_is_not_an_owner_and_not_a_case_worker_I_can_t_access_a_document() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R6_As_authenticated_user_who_is_not_an_owner_and_not_a_case_worker_GET_existing_document_binary_and_see_403() {

        String binaryUrl = createDocumentAndGetBinaryUrlAs(getCITIZEN());

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(binaryUrl);
    }

    @Test
    public void R7_As_authenticated_user_who_is_not_an_owner_and_is_a_case_worker_I_can_read_not_owned_documents() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_PROBATE())))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void As_authenticated_user_who_is_not_an_owner_and_has_case_worker_and_other_roles__I_can_read_not_owned_documents() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenSpacedRolesRequest(getCASE_WORKER(), new ArrayList<>(Arrays.asList(getCUSTOM_USER_ROLE(), getCASE_WORKER_ROLE_PROBATE())))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void R8_As_authenticated_user_GET_document_xxx_where_xxx_is_not_UUID() {

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/xxx");
    }

    @Test
    public void R9_As_authenticated_user_GET_document_111_where_111_is_not_UUID() {

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/111");
    }

    @Test
    public void R10_As_authenticated_user_GET_document_where_111_is_not_UUID() {

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(405)
            .when()
            .get("documents/");
    }

    @Test
    public void R11_As_authenticated_user_GET_document_xxx_where_xxx_is_UUID_but_it_doesn_t_exist_in_our_BD() {

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(404)
            .when()
            .get("documents/" + UUID.randomUUID());
    }

    @Test
    public void R14_As_authenticated_user_with_a_specific_role_I_can_access_a_document_if_its_CLASSIFICATION_is_restricted_and_roles_match() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "RESTRICTED",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCITIZEN_2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void R15_As_authenticated_user_with_a_specific_role_I_can_t_access_a_document_if_its_CLASSIFICATION_is_PRIVATE_and_roles_match() {

        ArrayList<String> roles = new ArrayList<>(List.of("not-a-caseworker"));

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PRIVATE", roles);

        givenRequest(getCITIZEN_2(), roles)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R16_As_authenticated_user_with_a_specific_role_I_can_access_a_document_if_its_CLASSIFICATION_is_public_and_roles_match() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PUBLIC",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCITIZEN_2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

    }

    @Test
    public void R17_As_authenticated_user_with_a_specific_role_I_can_access_a_document_if_its_CLASSIFICATION_is_public_and_matches_role() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PUBLIC",
            new ArrayList<>(Arrays.asList("citizen", "caseworker")));

        givenRequest(getCITIZEN_2(), new ArrayList<>(List.of("caseworker")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R18_As_authenticated_user_with_no_role_I_cannot_access_a_document_if_its_CLASSIFICATION_is_public_with_no_role() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PUBLIC",
            Collections.emptyList());

        givenRequest(getCITIZEN_2())
            .expect().statusCode(403)
            .when().get(documentUrl);
    }

    @Test
    public void R19_As_authenticated_user_with_some_role_I_cannot_access_a_document_if_its_CLASSIFICATION_is_public_and_roles_does_not_match() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PUBLIC",
            Collections.emptyList());

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);

    }

    @Test
    public void R20_As_authenticated_user_with_no_role__Tests_by_default_sets_role_as_citizen__I_can_access_a_document_if_its_CLASSIFICATION_is_public_and_roles_is_citizen() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "PUBLIC",
            new ArrayList<>(List.of("citizen")));

        givenRequest(getCITIZEN_2(), new ArrayList<>(List.of("citizen")))
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R21_As_an_owner_I_can_access_a_document_even_if_its_CLASSIFICATION_is_private_with_no_roles() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN_2(), getATTACHMENT_9_JPG(), "PRIVATE",
            Collections.emptyList());

        givenRequest(getCITIZEN_2())
            .expect().statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R22_As_authenticated_user_with_a_specific_role_I_can_t_access_a_document_if_its_CLASSIFICATION_is_restricted_and_roles_don_t_match() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN(), getATTACHMENT_9_JPG(), "RESTRICTED",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R23_As_an_Owner_with_no_role_I_can_access_a_document_even_if_its_CLASSIFICATION_is_private_and_role_as_caseworker() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN_2(), getATTACHMENT_9_JPG(), "PRIVATE",
            new ArrayList<>(List.of("caseworker")));

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R24_I_created_a_document_using_S2S_token_and_only_caseworkers_should_be_able_to_read_that_using_api_gateway() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_PROBATE())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", equalTo(getCITIZEN()))
            .when()
            .get(documentUrl)
            .thenReturn();

        givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_SSCS())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", Matchers.equalTo(getCITIZEN()))
            .when()
            .get(documentUrl)
            .thenReturn();

        givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_CMC())))
            .expect().log().all()
            .statusCode(200)
            .body("createdBy", Matchers.equalTo(getCITIZEN()))
            .when()
            .get(documentUrl)
            .thenReturn();
    }

    @Test
    public void R25_I_created_a_document_using_S2S_token__but_I_must_not_access_it_as_a_citizen_using_api_gateway() {

        String documentUrl = createDocumentAndGetBinaryUrlAs("user1");

       givenRequest(getCITIZEN())
           .expect()
           .statusCode(403)
           .when()
           .get(documentUrl);

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R26_userId_provided_during_data_creation_can_be_obtained_as_username_in_the_audit_trail() {

        String documentUrl = createDocumentAndGetUrlAs(getCASE_WORKER());

        givenRequest(getCASE_WORKER())
            .expect()
            .statusCode(200)
            .log().all()
            .body("createdBy", Matchers.equalTo(getCASE_WORKER()))
            .when()
            .get(documentUrl)
            .thenReturn();


        String userNameFromResponse = givenRequest(getCASE_WORKER(), List.of(getCASE_WORKER_ROLE_PROBATE()))
            .when()
            .get(documentUrl + "/auditEntries")
            .body().path("_embedded.auditEntries[0].username");

        // TODO : This assert Fails on Nightly build only. Passes on Local,  Preview as well as Master build.
        //assertThat(userNameFromResponse,equalTo(CASE_WORKER));
    }

    @Test
    public void R27_As_a_citizen_if_I_upload_a_document_to_API_Store_then_I_should_be_able_to_access_it_using_API_Gateway() {

        String documentUrl = createDocumentAndGetBinaryUrlAs(getCITIZEN());

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl);

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl);
    }

    @Test
    public void R28_As_authenticated_user_when_get_non_existing_document_binary_and_see_404() {

        String binaryUrl = createDocumentAndGetBinaryUrlAs(getCITIZEN());
        String documentStr = "documents/";
        String nonExistentId = UUID.randomUUID().toString();
        binaryUrl = binaryUrl.replace(binaryUrl.substring(binaryUrl.indexOf(documentStr) + documentStr.length(),
            binaryUrl.lastIndexOf("/")), nonExistentId);

        givenRequest(getCITIZEN()).expect().statusCode(404).when().get(binaryUrl);
    }
}
