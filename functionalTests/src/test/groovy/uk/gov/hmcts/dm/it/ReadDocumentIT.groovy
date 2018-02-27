package uk.gov.hmcts.dm.it

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.equalTo

/**
 * Created by pawel on 13/10/2017.
 */
@RunWith(SpringRunner.class)
class ReadDocumentIT extends BaseIT {

    @Before
    void setup() {
        createUser CITIZEN
    }

    @Test
    void "R1 As authenticated user who is an owner, can read owned documents"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
                .expect()
                .statusCode(200)
                .when()
                .get(documentUrl)

    }

    @Test
    void "R2 As authenticated user who is an owner, but Accept Header is application/vnd.uk.gov.hmcts.dm.document.v10000+hal+json"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .header('Accept','application/vnd.uk.gov.hmcts.dm.document.v10000+hal+json')
            .expect()
                .statusCode(406)
            .when()
                .get(documentUrl)

    }

    @Test
    void "R3 As unauthenticated user I try getting an existing document and get 401"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest()
            .expect()
                .statusCode(401)
            .when()
                .get(documentUrl)

    }


    @Test
    void "R4 As unauthenticated user GET existing document and receive 401"() {

        givenRequest()
            .expect()
                .statusCode(401)
            .when()
                .get('/documents/XXX')

    }

    @Test
    void "R5 As authenticated user who is not an owner and not a case worker I can't access a document"() {

        createUser CITIZEN_2

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .expect()
                .statusCode(403)
            .when()
                .get(documentUrl)

    }


    @Test
    void "R6 As authenticated user who is not an owner and not a case worker GET existing document binary and see 403"() {

        createUser CITIZEN_2

        def binaryUrl = createDocumentAndGetBinaryUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .expect()
                .statusCode(403)
            .when()
                .get(binaryUrl)

    }

    @Test
    void "R7 As authenticated user who is not an owner and is a case worker I can read not owned documents"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        createCaseWorker CASE_WORKER

        givenRequest(CASE_WORKER)
            .expect()
                .statusCode(200)
            .when()
                .get(documentUrl)

    }

    @Test
    void "R8 As authenticated user GET document/xxx where xxx is not UUID"() {

        givenRequest(CITIZEN)
            .expect()
                .statusCode(404)
            .when()
                .get('documents/xxx')
    }

    @Test
    void "R9 As authenticated user GET document/111 where 111 is not UUID"() {

        givenRequest(CITIZEN)
                .expect()
                .statusCode(404)
                .when()
                .get('documents/111')
    }

    @Test
    void "R10 As authenticated user GET document/ where 111 is not UUID"() {

        givenRequest(CITIZEN)
                .expect()
                .statusCode(405)
                .when()
                .get('documents/')
    }

    @Test
    void "R11 As authenticated user GET document/xxx where xxx is UUID but it doesn't exist in our BD"() {

        givenRequest(CITIZEN)
            .expect()
                .statusCode(404)
            .when()
                .get('documents/' + UUID.randomUUID())

    }

    @Test
    void "R12 As unauthenticated user GET document that exists with jwt parameter appended to the document URL"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        def jwt = authToken CITIZEN

        def response = givenRequest()
            .param("jwt", jwt)
            .redirects().follow(false)
            .expect()
                .statusCode(302)
            .when()
                .get(documentUrl).andReturn()

        def authToken = response.cookie('__auth-token')
        def newLocation = response.header('Location')

        givenRequest()
            .header('Authorization', authToken)
            .expect()
                .statusCode(200)
            .when()
                .get(newLocation)

    }


    @Test
    void "R13 As unauthenticated user GET document that does not exists with jwt parameter appended to the document URL"() {

        def jwt = authToken CITIZEN

        def response = givenRequest()
                .param("jwt", jwt)
                .redirects().follow(false)
                .expect()
                .statusCode(302)
                .when()
                .get('/documents/xxx').andReturn()

        def authToken = response.cookie('__auth-token')
        def newLocation = response.header('Location')

        givenRequest()
                .header('Authorization', authToken)
                .expect()
                .statusCode(404)
                .when()
                .get(newLocation)

    }

    @Test
    void "R14 As authenticated user with a specific role I can access a document if its CLASSIFICATION is restricted and roles match"() {

        createUser(CITIZEN_2, 'caseworker')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'RESTRICTED', ['caseworker']

        givenRequest(CITIZEN_2)
                .expect()
                .statusCode(200)
                .when()
                .get(documentUrl)

    }

    @Test
    void "R15 As authenticated user with a specific role I can't access a document if its CLASSIFICATION is PRIVATE and roles match"() {

        createUser(CITIZEN_2, 'caseworker')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PRIVATE', ['caseworker']

        givenRequest(CITIZEN_2)
                .expect()
                .statusCode(403)
                .when()
                .get(documentUrl)

    }

    @Test
    void "R16 As authenticated user with a specific role I can access a document if its CLASSIFICATION is public and roles match"() {

        createUser(CITIZEN_2, 'caseworker')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['caseworker']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R17 As authenticated user with a specific role I can access a document if its CLASSIFICATION is public and matches role"() {

        createUser(CITIZEN_2, 'caseworker')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['citizen', 'caseworker']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R18 As authenticated user with no role I cannot access a document if its CLASSIFICATION is public with no role"() {

        createUser(CITIZEN_2, null)

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R19 As authenticated user with some role I can access a document if its CLASSIFICATION is public and roles does not match"() {

        createUser(CITIZEN_2, 'caseworker')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R20 As authenticated user with no role (Tests by default sets role as citizen) I can access a document if its CLASSIFICATION is public and roles is citizen"() {

        createUser(CITIZEN_2, null)

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['citizen']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R21 As an owner I can access a document even if its CLASSIFICATION is private with no roles"() {

        createUser(CITIZEN_2, null)

        def documentUrl = createDocumentAndGetUrlAs CITIZEN_2, ATTACHMENT_9_JPG, 'PRIVATE', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R22 As authenticated user with a specific role I can't access a document if its CLASSIFICATION is restricted and roles don't match"() {

        createUser(CITIZEN_2, 'citizen')

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'RESTRICTED', ['caseworker']

        givenRequest(CITIZEN_2)
                .expect()
                .statusCode(403)
                .when()
                .get(documentUrl)
    }

    @Test
    void "R23 As an Owner with no role I can access a document even if its CLASSIFICATION is private and role as caseworker"() {

        createUser(CITIZEN_2, null)

        def documentUrl = createDocumentAndGetUrlAs CITIZEN_2, ATTACHMENT_9_JPG, 'PRIVATE', ['caseworker']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R24 I created a document using S2S token and only caseworkers should be able to read that using api gateway"() {

        def userId = "user2"
        def documentUrl = createDocumentUsingS2STokenAndUserId userId
        createCaseWorker CASE_WORKER

        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(userId))
            .statusCode(200)
            .when()
            .get(documentUrl)

        createCaseWorkerCMC CASE_WORKER
        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(userId))
            .statusCode(200)
            .when()
            .get(documentUrl)

        createCaseWorkerSSCS CASE_WORKER
        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(userId))
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R25 I created a document using S2S token, but I must not access it as a citizen using api gateway"() {

        createUser CITIZEN

        def documentUrl = createDocumentUsingS2STokenAndUserId("user1")

        givenRequest(CITIZEN)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

        createUser CITIZEN_2

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R26 userId provided during data creation can be obtained as username in the audit trail"() {

        createUser(CASE_WORKER)

        def token = authToken CASE_WORKER
        def userid = userId token
        def documentUrl = createDocumentUsingS2STokenAndUserId userid

        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(userid))
            .statusCode(200)
            .when()
            .get(documentUrl)

        Map<String, String> map = givenRequest(CASE_WORKER)
            .when()
            .get(documentUrl + "/auditEntries")
            .path("_embedded.auditEntries[0]")

        Assert.assertEquals(map.get("username"), userid)
    }

    @Test
    void "R27 As a citizen if I upload a document to API Store then I should be able to access it using API Gateway"() {

        createUser CITIZEN
        def token = authToken CITIZEN
        def userid = userId token

        def documentUrl = createDocumentUsingS2STokenAndUserId userid

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

        createUser CITIZEN_2

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }
}
