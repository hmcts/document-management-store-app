package uk.gov.hmcts.dm.functional;

import groovy.json.JsonOutput;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.Collections;
import java.util.LinkedHashMap;

public class DeleteDocumentsIT extends BaseIT {
    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void s1AsAuthenticatedUserICanDeleteDocumentsForASpecificCaseThatHasDocuments() {

        String caseNo1 = "1234567890123456";

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("case_id", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map);
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("case_id", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map1);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("case_id", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map2);

        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("caseRef", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map3))
            .expect()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("caseDocumentsFound", Matchers.equalTo(3))
            .body("markedForDeletion", Matchers.equalTo(3))
            .when()
            .post("/documents/delete");

    }

    @Test
    public void s2AsAuthenticatedUserICanNotDeleteDocumentsForASpecificCaseThatDoesNotHaveAnyDocuments() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON).body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("caseDocumentsFound", Matchers.equalTo(0))
            .body("markedForDeletion", Matchers.equalTo(0))
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s3AsAuthenticatedUserIReceiveAnErrorForIncorrectlyPostedDeleteCriteria() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("xyz", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s4AsAuthenticatedUserIReceiveABadRequestForEmptyDeleteCriteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", "");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s5AsAuthenticatedUserIReceiveABadRequestForInvalidDeleteCriteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", "xyz4567890123456");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all().statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s6AsAuthenticatedUserIReceiveABadRequestForShortCaseRef() {

        String caseNo1 = RandomStringUtils.randomNumeric(15);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s7AsAuthenticatedUserIReceiveABadRequestForLongCaseRef() {

        String caseNo1 = RandomStringUtils.randomNumeric(17);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void s8AsAnUnauthenticatedUserIAmForbiddenToInvokeDocumentsDelete() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(401)
            .when()
            .post("/documents/delete");
    }
}
