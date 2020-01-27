package uk.gov.hmcts.dm.functional

import io.restassured.http.ContentType
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assume.assumeTrue

@RunWith(SpringRunner.class)
class UpdateDocumentIT  extends BaseIT {

    @Test
    void "UD1 update TTL for a Document"() {
        assumeTrue(toggleConfiguration.isTtl())

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect()
                .statusCode(200)
                .body("ttl", equalTo("3000-10-31T10:10:10+0000"))
            .when()
                .patch(documentUrl)

    }

    @Test
    void "UD2 fail to update TTL for a Document that I don't own"() {
        assumeTrue(toggleConfiguration.isTtl())

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect()
                .statusCode(403)
            .when()
                .patch(documentUrl)

    }

    @Test
    void "UD3 fail to update TTL for a Document by a caseworker"() {
        assumeTrue(toggleConfiguration.isTtl())

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CASE_WORKER)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(403)
            .when()
            .patch(documentUrl)
    }

    @Test
    void "UD4 when updating a ttl the last ttl will be taken into consideration (if more than one are in a body)"() {
        assumeTrue(toggleConfiguration.isTtl())

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .body([ttl: "3000-01-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", equalTo("3000-01-31T10:10:10+0000"))
            .when()
            .patch(documentUrl)
    }

    @Test
    void "UD5 TTL will stay intact if a patch request is made without a new TTL in the body"() {
        assumeTrue(toggleConfiguration.isTtl())

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(200)
            .body("ttl", equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .patch(documentUrl)
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(400)
            .body("ttl", equalTo(null))
            .when()
            .patch(documentUrl)

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .body("ttl", equalTo("3000-10-31T10:10:10+0000"))
            .when()
            .get(documentUrl)
    }
}
