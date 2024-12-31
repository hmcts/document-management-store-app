package uk.gov.hmcts.dm.functional;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class UpdateDocumentIT extends BaseIT {

    @Test
    public void ud1UpdateTtlForADocument() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .patch(documentUrl);
    }

    @Test
    public void ud2FailToUpdateTtlForADocumentThatIDonTOwn() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCitizen2())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(403)
            .when()
            .patch(documentUrl);
    }

    @Test
    public void ud3FailToUpdateTtlForADocumentByACaseworker() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCaseWorker())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(403)
            .when()
            .patch(documentUrl);
    }

    @Test
    public void ud4WhenUpdatingATtlTheLastTtlWillBeTakenIntoConsiderationIfMoreThanOneAreInABody() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("ttl", "3000-01-31T10:10:10+0000");
        givenRequest(getCitizen())
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
    public void ud5TtlWillStayIntactIfAPatchRequestIsMadeWithoutANewTtlInTheBody() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .patch(documentUrl);

        givenRequest(getCitizen())
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(400)
            .body("ttl", Matchers.equalTo(null))
            .when()
            .patch(documentUrl);

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .get(documentUrl);
    }

    @Test
    public void ud6InvalidBulkUpdateRequest() {
        String document1Url = createDocumentAndGetUrlAs(getCitizen());
        String[] split = document1Url
            .split("/");
        String documentId = split[split.length - 1];
        String document2Url = createDocumentAndGetUrlAs(getCitizen());
        split = document2Url
            .split("/");
        final String documentId2 = split[split.length - 1];


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
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(500)
            .when()
            .patch("/documents");
    }

    @Test
    public void ud7ValidBulkUpdateRequest() {
        String document1Url = createDocumentAndGetUrlAs(getCitizen());
        String[] split = document1Url
            .split("/");
        String documentId = split[split.length - 1];
        String document2Url = createDocumentAndGetUrlAs(getCitizen());
        split = document2Url
            .split("/");
        final String documentId2 = split[split.length - 1];

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
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(200)
            .body("result", Matchers.equalTo("Success"))
            .when()
            .patch("/documents");
    }

    @Test
    public void ud8PartialBulkUpdateRequestSuccess() {
        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        final UUID uuid = UUID.randomUUID();

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
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .body("error", Matchers.equalTo("Document with ID: " + uuid + " could not be found"))
            .when()
            .patch("/documents");
    }

    @Test
    public void ud9PartialUpdateTtlForANonExistentDocument() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        String nonExistentId = UUID.randomUUID().toString();
        documentUrl = documentUrl.replace(documentId, nonExistentId);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("ttl", "3000-10-31T10:10:10+0000");
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .when()
            .patch(documentUrl);
    }
}
