package uk.gov.hmcts.dm.functional;

import groovy.json.JsonOutput;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class SearchDocumentIT extends BaseIT {

    private static final String DOCUMENTS_FILTER_ENDPOINT = "/documents/filter";
    private static final String VALUE_CONST = "value";

    @Test
    public void s1AsAuthenticatedUserICanSearchForDocumentUsingSpecificMetadataProperty() {

        final String caseNo1 = RandomStringUtils.randomAlphabetic(50);
        final String caseNo2 = RandomStringUtils.randomAlphabetic(50);

        Map<String, String> map = Map.of("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map);
        Map<String, String> map1 = Map.of("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map1);
        Map<String, String> map2 = Map.of("case", caseNo1);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map2);
        Map<String, String> map3 = Map.of("case", caseNo2);
        createDocument(getCitizen(), null, null, Collections.emptyList(), map3);

        Map<String, String> map4 = Map.of("name", "case", VALUE_CONST, caseNo1);
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map4))
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE_VALUE)
            .body("_embedded.documents.size()", Matchers.is(3))
            .when()
            .post(DOCUMENTS_FILTER_ENDPOINT);

    }

    @Test
    public void s2AsAuthenticatedUserIReceiveErrorForIncorrectlyPostedSearchCriteria() {
        Map<String, String> map = Map.of("name", "case");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(422)
            .body("error", Matchers.equalTo("must not be null"))
            .when().post(DOCUMENTS_FILTER_ENDPOINT);
    }

    @Test
    public void s3AsUnauthenticatedUserIAmForbiddenToInvokeSearch() {
        Map<String, String> map = Map.of("name", "case", VALUE_CONST, "123");
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(403)
            .when()
            .post(DOCUMENTS_FILTER_ENDPOINT);
    }

    @Test
    public void s4AsAuthenticatedUserIReceiveNoRecordsWhenSearchedItemCouldNotBeFound() {
        Map<String, String> map = Map.of("name", "case", VALUE_CONST, "123");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .body("page.totalElements", Matchers.is(0))
            .when()
            .post(DOCUMENTS_FILTER_ENDPOINT);
    }

    @Test
    public void s4AsAAuthenticatedUserICanSearchUsingSpecialCharacters() {
        Map<String, String> map =
            Map.of("name", "case", VALUE_CONST, "!\"£$%%^&*()<>:@~[];'#,./ÄÖÜẞ▒¶§■¾±≡µÞÌ█ð╬¤╠┼▓®¿ØÆ");
        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson(map))
            .expect().log().all()
            .statusCode(200)
            .when()
            .post(DOCUMENTS_FILTER_ENDPOINT);
    }
}
