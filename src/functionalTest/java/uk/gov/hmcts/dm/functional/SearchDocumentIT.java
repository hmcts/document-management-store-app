package uk.gov.hmcts.dm.functional;

import groovy.json.JsonOutput;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;

public class SearchDocumentIT extends BaseIT {

    @Test
    public void s1AsAuthenticatedUserICanSearchForDocumentUsingSpecificMetadataProperty() {

        final String caseNo1 = RandomStringUtils.randomAlphabetic(50);
        final String caseNo2 = RandomStringUtils.randomAlphabetic(50);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map);
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map1);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map2);
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("case", caseNo2);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map3);

        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(2);
        map4.put("name", "case");
        map4.put("value", caseNo1);
        givenRequest(getCitizen())
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
    public void s2AsAuthenticatedUserIReceiveErrorForIncorrectlyPostedSearchCriteria() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("name", "case");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when().post("/documents/filter");
    }

    @Test
    public void s3AsUnauthenticatedUserIAmForbiddenToInvokeSearch() {
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
    public void s4AsAuthenticatedUserIReceiveNoRecordsWhenSearchedItemCouldNotBeFound() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("name", "case");
        map.put("value", "123");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("page.totalElements", Matchers.is(0))
            .when()
            .post("/documents/filter");
    }

    @Test
    public void s4AsAAuthenticatedUserICanSearchUsingSpecialCharacters() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("name", "case");
        map.put("value", "!\"£$%%^&*()<>:@~[];'#,./ÄÖÜẞ▒¶§■¾±≡µÞÌ█ð╬¤╠┼▓®¿ØÆ");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .when()
            .post("/documents/filter");
    }
}
