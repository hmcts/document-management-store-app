package uk.gov.hmcts.dm.functional

import groovy.json.JsonOutput
import io.restassured.http.ContentType
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Rule
import org.junit.Test
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo

class DeleteDocumentsIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    void "S1 As authenticated user I can delete documents for a specific case that has documents"() {

        def caseNo1 = '1234567890123456'

        createDocument CITIZEN, null, null, null, [case_id: caseNo1]
        createDocument CITIZEN, null, null, null, [case_id: caseNo1]
        createDocument CITIZEN, null, null, null, [case_id: caseNo1]

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: caseNo1]))
            .expect()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("caseDocumentsFound", equalTo(3))
            .body("markedForDeletion", equalTo(3))
            .when()
            .post('/documents/delete')

    }

    @Test
    void "S2 As authenticated user I can not delete documents for a specific case that does not have any documents"() {

        def caseNo1 = RandomStringUtils.randomNumeric(16)

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: caseNo1]))
            .expect().log().all()
            .statusCode(200)
            .body("caseDocumentsFound", equalTo(0))
            .body("markedForDeletion", equalTo(0))
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S3 As authenticated user I receive an error for incorrectly posted delete criteria"() {

        def caseNo1 = RandomStringUtils.randomNumeric(16)

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([xyz: caseNo1]))
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("must not be null"))
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S4 As authenticated user I receive a bad request for empty delete criteria"() {
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: '']))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S5 As authenticated user I receive a bad request for invalid delete criteria"() {
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: 'xyz4567890123456']))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S6 As authenticated user I receive a bad request for short case ref"() {

        def caseNo1 = RandomStringUtils.randomNumeric(15)

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: caseNo1]))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S7 As authenticated user I receive a bad request for long case ref"() {

        def caseNo1 = RandomStringUtils.randomNumeric(17)

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: caseNo1]))
            .expect().log().all()
            .statusCode(400)
            .when()
            .post('/documents/delete')
    }

    @Test
    void "S8 As an unauthenticated user I am forbidden to invoke documents delete"() {

        def caseNo1 = RandomStringUtils.randomNumeric(16)

        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([caseRef: caseNo1]))
            .expect().log().all()
            .statusCode(403)
            .when()
            .post('/documents/delete')
    }

}
