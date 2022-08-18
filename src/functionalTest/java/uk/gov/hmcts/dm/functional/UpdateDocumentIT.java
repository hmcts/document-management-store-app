package uk.gov.hmcts.dm.functional;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.Serializable;
import java.util.*;

public class UpdateDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void UD1_update_TTL_for_a_Document() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .patch(documentUrl);
    }

    @Test
    public void UD2_fail_to_update_TTL_for_a_Document_that_I_don_t_own() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCITIZEN_2())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(403)
            .when()
            .patch(documentUrl);
    }

    @Test
    public void UD3_fail_to_update_TTL_for_a_Document_by_a_caseworker() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCASE_WORKER())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(403)
            .when()
            .patch(documentUrl);
    }

    @Test
    public void UD4_when_updating_a_ttl_the_last_ttl_will_be_taken_into_consideration__if_more_than_one_are_in_a_body_() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("ttl", "3000-01-31T10:10:10+0000");
        givenRequest(getCITIZEN())
            .body(map)
            .body(map1)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-01-31T10:10:10+0000"))
            .when()
            .patch(documentUrl);
    }

    @Test
    public void UD5_TTL_will_stay_intact_if_a_patch_request_is_made_without_a_new_TTL_in_the_body() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .patch(documentUrl);

        givenRequest(getCITIZEN())
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(400)
            .body("ttl", Matchers.equalTo(null))
            .when()
            .patch(documentUrl);

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .get(documentUrl);
    }

    @Test
    public void UD6_invalid_bulk_update_request() {
        String document1Url = createDocumentAndGetUrlAs(getCITIZEN());
        String[] split = document1Url
            .split("/");
        String documentId = split[split.length - 1];
        String document2Url = createDocumentAndGetUrlAs(getCITIZEN());
        split = document2Url
            .split("/");
        String documentId2 = split[split.length - 1];


        LinkedHashMap<String, Serializable> map = new LinkedHashMap<>(2);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>(2);
        map1.put("id", documentId);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("metakey", "metavalue");
        map1.put("metadata", map2);
        LinkedHashMap<String, Object> map3 = new LinkedHashMap<>(2);
        map3.put("id", documentId2);
        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(1);
        map4.put("metakey2", "metavalue2");
        map3.put("metadata", map4);
        map.put("documents", new ArrayList<>(Arrays.asList(map1, map3)));
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(500)
            .when()
            .patch("/documents");
    }

    @Test
    public void UD7_valid_bulk_update_request() {
        String document1Url = createDocumentAndGetUrlAs(getCITIZEN());
        String[] split = document1Url
            .split("/");
        String documentId = split[split.length - 1];
        String document2Url = createDocumentAndGetUrlAs(getCITIZEN());
        split = document2Url
            .split("/");
        String documentId2 = split[split.length - 1];

        LinkedHashMap<String, Serializable> map = new LinkedHashMap<>(2);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>(2);
        map1.put("documentId", documentId);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("metakey", "metavalue");
        map1.put("metadata", map2);
        LinkedHashMap<String, Object> map3 = new LinkedHashMap<>(2);
        map3.put("documentId", documentId2);
        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(1);
        map4.put("metakey2", "metavalue2");
        map3.put("metadata", map4);
        map.put("documents", new ArrayList<>(Arrays.asList(map1, map3)));
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(200)
            .body("result", Matchers.equalTo("Success"))
            .when()
            .patch("/documents");
    }

    @Test
    public void UD8_partial_bulk_update_request_success() {
        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        UUID uuid = UUID.randomUUID();

        LinkedHashMap<String, Serializable> map = new LinkedHashMap<>(2);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        LinkedHashMap<String, Serializable> map1 = new LinkedHashMap<>(2);
        map1.put("documentId", documentId);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("metakey", "metavalue");
        map1.put("metadata", map2);
        LinkedHashMap<String, Serializable> map3 = new LinkedHashMap<>(2);
        map3.put("documentId", uuid);
        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(1);
        map4.put("metakey2", "metavalue2");
        map3.put("metadata", map4);
        map.put("documents", new ArrayList<>(List.of(map1, map3)));
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .body("error", Matchers.equalTo("Document with ID: " + uuid + " could not be found"))
            .when()
            .patch("/documents");
    }

    @Test
    public void UD9_partial_update_TTL_for_a_non_existent_document() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        String nonExistentId = UUID.randomUUID().toString();
        documentUrl = documentUrl.replace(documentId, nonExistentId);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCITIZEN())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .when()
            .patch(documentUrl);
    }
}
