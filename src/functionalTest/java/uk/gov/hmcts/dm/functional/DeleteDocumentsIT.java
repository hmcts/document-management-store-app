package uk.gov.hmcts.dm.functional;

import groovy.json.JsonOutput;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;

public class DeleteDocumentsIT extends BaseIT {

    private static final String CASE_ID_CONST = "case_id";
    private static final String CASE_REF_CONST = "caseRef";
    private static final String DELETE_PATH_CONST = "/documents/delete";

    @Test
    public void s1AsAuthenticatedUserICanDeleteDocumentsForASpecificCaseThatHasDocuments() {

        String caseNo1 = "1234567890123456";

        Map<String, String> map = Map.of(CASE_ID_CONST, caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map);
        Map<String, String> map1 = Map.of(CASE_ID_CONST, caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map1);
        Map<String, String> map2 = Map.of(CASE_ID_CONST, caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map2);

        Map<String, String> map3 = Map.of(CASE_REF_CONST, caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map3))
            .expect()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("caseDocumentsFound", Matchers.equalTo(3))
            .body("markedForDeletion", Matchers.equalTo(3))
            .when()
            .post(DELETE_PATH_CONST);

    }

    @Test
    public void s2AsAuthenticatedUserICanNotDeleteDocumentsForASpecificCaseThatDoesNotHaveAnyDocuments() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        Map<String, String> map = Map.of(CASE_REF_CONST, caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON).body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("caseDocumentsFound", Matchers.equalTo(0))
            .body("markedForDeletion", Matchers.equalTo(0))
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s3AsAuthenticatedUserIReceiveAnErrorForIncorrectlyPostedDeleteCriteria() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        Map<String, String> map = Map.of("xyz", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s4AsAuthenticatedUserIReceiveABadRequestForEmptyDeleteCriteria() {
        Map<String, String> map = Map.of(CASE_REF_CONST, "");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s5AsAuthenticatedUserIReceiveABadRequestForInvalidDeleteCriteria() {
        Map<String, String> map = Map.of(CASE_REF_CONST, "xyz4567890123456");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all().statusCode(400)
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s6AsAuthenticatedUserIReceiveABadRequestForShortCaseRef() {

        String caseNo1 = RandomStringUtils.randomNumeric(15);

        Map<String, String> map = Map.of(CASE_REF_CONST, caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s7AsAuthenticatedUserIReceiveABadRequestForLongCaseRef() {

        String caseNo1 = RandomStringUtils.randomNumeric(17);

        Map<String, String> map = Map.of(CASE_REF_CONST, caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post(DELETE_PATH_CONST);
    }

    @Test
    public void s8AsAnUnauthenticatedUserIAmForbiddenToInvokeDocumentsDelete() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        Map<String, String> map = Map.of(CASE_REF_CONST, caseNo1);
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(403)
            .when()
            .post(DELETE_PATH_CONST);
    }
}
