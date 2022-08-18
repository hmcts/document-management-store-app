package uk.gov.hmcts.dm.functional;

import groovy.json.JsonOutput;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.Collections;
import java.util.LinkedHashMap;

public class SearchDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void S1_As_authenticated_user_I_can_search_for_document_using_specific_metadata_property() {

        String caseNo1 = RandomStringUtils.randomAlphabetic(50);
        String caseNo2 = RandomStringUtils.randomAlphabetic(50);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("case", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map);
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("case", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map1);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("case", caseNo1);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map2);
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("case", caseNo2);
        createDocument(getCITIZEN(), null, null, Collections.emptyList(), map3);

        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(2);
        map4.put("name", "case");
        map4.put("value", caseNo1);
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map4))
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE_VALUE)
            .body("_embedded.documents.size()", Matchers.is(3))
            .when()
            .post("/documents/filter");

    }

    @Test
    public void S2_As_authenticated_user_I_receive_error_for_incorrectly_posted_search_criteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("name", "case");
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when().post("/documents/filter");
    }

    @Test
    public void S3_As_unauthenticated_user_I_am_forbidden_to_invoke_search() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("name", "case");
        map.put("value", "123");
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(403)
            .when()
            .post("/documents/filter");
    }

    @Test
    public void S4_As_authenticated_user_I_receive_no_records_when_searched_item_could_not_be_found() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("name", "case");
        map.put("value", "123");
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("page.totalElements", Matchers.is(0))
            .when()
            .post("/documents/filter");
    }

    @Test
    public void S4_As_a_authenticated_user_I_can_search_using_special_characters() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("name", "case");
        map.put("value", "!\"£$%%^&*()<>:@~[];'#,./ÄÖÜẞ▒¶§■¾±≡µÞÌ█ð╬¤╠┼▓®¿ØÆ");
        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .when()
            .post("/documents/filter");
    }
}
