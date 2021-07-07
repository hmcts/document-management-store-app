package uk.gov.hmcts.dm.functional

import io.restassured.http.ContentType
import org.junit.Rule
import org.junit.Test
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assume.assumeTrue

class UpdateDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "UD1 update TTL for a Document"() {
        assumeTrue(toggleTtlEnabled)

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
        assumeTrue(toggleTtlEnabled)

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
        assumeTrue(toggleTtlEnabled)

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
        assumeTrue(toggleTtlEnabled)

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
        assumeTrue(toggleTtlEnabled)

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

    @Test
    void "UD6 invalid bulk update request"() {

        def documentUrl = createDocumentAndGetUrlAs(CITIZEN)
        def documentUrl2 = createDocumentAndGetUrlAs(CITIZEN)
        def documentId = documentUrl.split("/").last()
        def documentId2 = documentUrl2.split("/").last()

        givenRequest(CITIZEN)
            .body([
                ttl      : "3000-10-31T10:10:10+0000",
                documents: [
                    [id: documentId, metadata: [metakey: "metavalue"]],
                    [id: documentId2, metadata: [metakey2: "metavalue2"]]
                ]
            ])
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(500)//FIXME should be 400
            .when()
            .patch("/documents")
    }

    @Test
    void "UD7 valid bulk update request"() {

        def documentUrl = createDocumentAndGetUrlAs(CITIZEN)
        def documentUrl2 = createDocumentAndGetUrlAs(CITIZEN)
        def documentId = documentUrl.split("/").last()
        def documentId2 = documentUrl2.split("/").last()

        givenRequest(CITIZEN)
            .body([
                ttl      : "3000-10-31T10:10:10+0000",
                documents: [
                    [documentId: documentId, metadata: [metakey: "metavalue"]],
                    [documentId: documentId2, metadata: [metakey2: "metavalue2"]]
                ]
            ])
            .contentType(ContentType.JSON)
            .expect()
            .log().all()
            .statusCode(200)
            .body("result", equalTo("Success"))
            .when()
            .patch("/documents")
    }


    @Test
    void "UD8 partial bulk update request success"() {

        def documentUrl = createDocumentAndGetUrlAs(CITIZEN)
        def documentId = documentUrl.split("/").last()
        def uuid = UUID.randomUUID()

        givenRequest(CITIZEN)
            .body([
                ttl      : "3000-10-31T10:10:10+0000",
                documents: [
                    [documentId: documentId, metadata: [metakey: "metavalue"]],
                    [documentId: uuid, metadata: [metakey2: "metavalue2"]]
                ]
            ])
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(500)//FIXME should be 404
            .body("error", equalTo("Document with ID: " + uuid + " could not be found"))
            .when()
            .patch("/documents")
    }

    @Test
    void "UD9 partial update TTL for a non existent document"() {
        assumeTrue(toggleTtlEnabled)

        String documentUrl = createDocumentAndGetUrlAs CITIZEN
        String documentId = documentUrl.split("/").last()
        String nonExistentId = UUID.randomUUID().toString()
        documentUrl = documentUrl.replace(documentId, nonExistentId)

        givenRequest(CITIZEN)
            .body([ttl: "3000-10-31T10:10:10+0000"])
            .contentType(ContentType.JSON)
            .expect().log().all()
            .statusCode(404)
            .when()
            .patch(documentUrl)
    }
}
