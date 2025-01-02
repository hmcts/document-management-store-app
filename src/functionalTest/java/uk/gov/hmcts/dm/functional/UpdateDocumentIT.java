package uk.gov.hmcts.dm.functional;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpdateDocumentIT extends BaseIT {


    private static final String METADATA_CONST = "metadata";
    private static final String DOCUMENTS_CONST = "documents";
    private static final String DOCUMENT_ID_CONST = "documentId";
    private static final String DOCUMENTS_PATH = "/documents";
    private static final String META_KEY2_CONST = "metakey2";
    private static final String META_KEY2_VALUE = "metavalue2";

    private static final String META_KEY_CONST = "metakey";
    private static final String META_KEY_VALUE = "metavalue";

    private static final String FUTURE_DATE_3000 = "3000-10-31T10:10:10+0000";

    @Test
    public void ud1UpdateTtlForADocument() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo(FUTURE_DATE_3000))
            .when()
            .patch(documentUrl);
    }

    @Test
    public void ud2FailToUpdateTtlForADocumentThatIDonTOwn() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
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

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
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

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
        Map<String, String> map1 = Map.of("ttl", FUTURE_DATE_3000);
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

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", Matchers.equalTo(FUTURE_DATE_3000))
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
            .body("ttl", Matchers.equalTo(FUTURE_DATE_3000))
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



        Map<String, String> map2 = Map.of(META_KEY_CONST, META_KEY_VALUE);
        Map<String, Object> map1 = Map.of("id", documentId, METADATA_CONST, map2);

        Map<String, String> map4 = Map.of(META_KEY2_CONST, META_KEY2_VALUE);
        Map<String, Object> map3 = Map.of("id", documentId2, METADATA_CONST, map4);

        Map<String, Serializable> map =
            Map.of("ttl", FUTURE_DATE_3000,
                DOCUMENTS_CONST, new ArrayList<>(Arrays.asList(map1, map3)));

        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(500)
            .when()
            .patch(DOCUMENTS_PATH);
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

        Map<String, String> map2 = Map.of(META_KEY_CONST, META_KEY_VALUE);
        Map<String, Object> map1 = Map.of(DOCUMENT_ID_CONST, documentId, METADATA_CONST, map2);

        Map<String, String> map4 = Map.of(META_KEY2_CONST, META_KEY2_VALUE);
        Map<String, Object> map3 = Map.of(DOCUMENT_ID_CONST, documentId2, METADATA_CONST, map4);

        Map<String, Serializable> map
            = Map.of("ttl", FUTURE_DATE_3000, DOCUMENTS_CONST, new ArrayList<>(Arrays.asList(map1, map3)));
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(200)
            .body("result", Matchers.equalTo("Success"))
            .when()
            .patch(DOCUMENTS_PATH);
    }

    @Test
    public void ud8PartialBulkUpdateRequestSuccess() {
        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        final UUID uuid = UUID.randomUUID();

        LinkedHashMap<String, Serializable> map = new LinkedHashMap<>(2);
        map.put("ttl", FUTURE_DATE_3000);
        LinkedHashMap<String, Serializable> map1 = new LinkedHashMap<>(2);
        map1.put(DOCUMENT_ID_CONST, documentId);
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put(META_KEY_CONST, META_KEY_VALUE);
        map1.put(METADATA_CONST, map2);
        LinkedHashMap<String, Serializable> map3 = new LinkedHashMap<>(2);
        map3.put(DOCUMENT_ID_CONST, uuid);
        LinkedHashMap<String, String> map4 = new LinkedHashMap<>(1);
        map4.put(META_KEY2_CONST, META_KEY2_VALUE);
        map3.put(METADATA_CONST, map4);
        map.put(DOCUMENTS_CONST, new ArrayList<>(List.of(map1, map3)));
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .body("error", Matchers.equalTo("Document with ID: " + uuid + " could not be found"))
            .when()
            .patch(DOCUMENTS_PATH);
    }

    @Test
    public void ud9PartialUpdateTtlForANonExistentDocument() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());
        String[] split = documentUrl
            .split("/");
        String documentId = split[split.length - 1];
        String nonExistentId = UUID.randomUUID().toString();
        documentUrl = documentUrl.replace(documentId, nonExistentId);

        Map<String, String> map = Map.of("ttl", FUTURE_DATE_3000);
        givenRequest(getCitizen())
            .body(map)
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .when()
            .patch(documentUrl);
    }
}
