package uk.gov.hmcts.dm.functional

import groovy.json.JsonOutput
import io.restassured.http.ContentType
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import static org.hamcrest.Matchers.equalTo


@RunWith(SpringRunner.class)
class SearchDocumentIT extends BaseIT {

    @Test
    void "S1 As authenticated user I can search for document using specific metadata property"() {

        def caseNo1 = RandomStringUtils.randomAlphabetic(50)
        def caseNo2 = RandomStringUtils.randomAlphabetic(50)

        createDocument CITIZEN, null, null, null, [case:caseNo1]
        createDocument CITIZEN, null, null, null, [case:caseNo1]
        createDocument CITIZEN, null, null, null, [case:caseNo1]
        createDocument CITIZEN, null, null, null, [case:caseNo2]

        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([name:'case', value:caseNo1]))
        .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE_VALUE)
            .body("_embedded.documents.size()", Matchers.is(3))
        .when()
            .post('/documents/filter')

    }

    @Test
    void "S2 As authenticated user I receive error for incorrectly posted search criteria"() {
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([name:'case']))
        .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("may not be null"))
            .when()
        .post('/documents/filter')
    }

    @Test
    void "S3 As unauthenticated user I am forbidden to invoke search"() {
        givenUnauthenticatedRequest()
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([name:'case', value:'123']))
        .expect().log().all()
            .statusCode(403)
            .when()
        .post('/documents/filter')
    }

    @Test
    void "S4 As authenticated user I receive no records when searched item could not be found"() {
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([name:'case', value:'123']))
            .expect().log().all()
            .statusCode(200)
            .body("page.totalElements", equalTo(0))
            .when()
            .post('/documents/filter')
    }

    @Test
    void "S4 As a authenticated user I can search using special characters"() {
        givenRequest(CITIZEN)
            .contentType(ContentType.JSON)
            .body(JsonOutput.toJson([name:'case', value:'!"£$%%^&*()<>:@~[];\'#,./ÄÖÜẞ▒¶§■¾±≡µÞÌ█ð╬¤╠┼▓®¿ØÆ']))
            .expect().log().all()
            .statusCode(200)
            .when()
            .post('/documents/filter')
    }
}
