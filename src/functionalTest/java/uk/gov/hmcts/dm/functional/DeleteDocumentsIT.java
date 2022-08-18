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

public class DeleteDocumentsIT extends BaseIT{
    @Rule
    public RetryRule retryRule = new RetryRule(3);
    @Test
    public void S1_As_authenticated_user_I_can_delete_documents_for_a_specific_case_that_has_documents() {

        String caseNo1 = "1234567890123456";

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("case_id", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map);
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("case_id", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map1);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("case_id", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map2);

        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("caseRef", caseNo1);
        givenRequest(getCITIZEN())
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
    public void S2_As_authenticated_user_I_can_not_delete_documents_for_a_specific_case_that_does_not_have_any_documents() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON).body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("caseDocumentsFound", Matchers.equalTo(0))
            .body("markedForDeletion", Matchers.equalTo(0))
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S3_As_authenticated_user_I_receive_an_error_for_incorrectly_posted_delete_criteria() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("xyz", caseNo1);
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S4_As_authenticated_user_I_receive_a_bad_request_for_empty_delete_criteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", "");
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S5_As_authenticated_user_I_receive_a_bad_request_for_invalid_delete_criteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", "xyz4567890123456");
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all().statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S6_As_authenticated_user_I_receive_a_bad_request_for_short_case_ref() {

        String caseNo1 = RandomStringUtils.randomNumeric(15);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S7_As_authenticated_user_I_receive_a_bad_request_for_long_case_ref() {

        String caseNo1 = RandomStringUtils.randomNumeric(17);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post("/documents/delete");
    }

    @Test
    public void S8_As_an_unauthenticated_user_I_am_forbidden_to_invoke_documents_delete() {

        String caseNo1 = RandomStringUtils.randomNumeric(16);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("caseRef", caseNo1);
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(403)
            .when()
            .post("/documents/delete");
    }
}
