package uk.gov.hmcts.dm.functional


import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Disabled
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo

@Disabled
class ReadDocumentIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

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
    void "R2 As authenticated user who is an owner, but Accept Header is application hal json"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN)
            .header('Accept', 'application/vnd.uk.gov.hmcts.dm.document.v10000+hal+json')
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R3 As unauthenticated user I try getting an existing document and get 403"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

    }


    @Test
    void "R4 As unauthenticated user GET existing document and receive 403"() {

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .when()
            .get('/documents/XXX')

    }

    @Test
    void "R5 As authenticated user who is not an owner and not a case worker I can't access a document"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

    }


    @Test
    void "R6 As authenticated user who is not an owner and not a case worker GET existing document binary and see 403"() {

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

        givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_PROBATE])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "As authenticated user who is not an owner and has case worker and other roles  I can read not owned documents"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenSpacedRolesRequest(CASE_WORKER, [CUSTOM_USER_ROLE, CASE_WORKER_ROLE_PROBATE])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R8 As authenticated user GET document xxx where xxx is not UUID"() {

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get('documents/xxx')
    }

    @Test
    void "R9 As authenticated user GET document 111 where 111 is not UUID"() {

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get('documents/111')
    }

    @Test
    void "R10 As authenticated user GET document where 111 is not UUID"() {

        givenRequest(CITIZEN)
            .expect()
            .statusCode(405)
            .when()
            .get('documents/')
    }

    @Test
    void "R11 As authenticated user GET document xxx where xxx is UUID but it doesn't exist in our BD"() {

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get('documents/' + UUID.randomUUID())

    }

    @Test
    void "R14 As authenticated user with a specific role I can access a document if its CLASSIFICATION is restricted and roles match"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'RESTRICTED', ['caseworker']

        givenRequest(CITIZEN_2, ['caseworker'])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R15 As authenticated user with a specific role I can't access a document if its CLASSIFICATION is PRIVATE and roles match"() {

        def roles = ['not-a-caseworker']

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PRIVATE', roles

        givenRequest(CITIZEN_2, roles)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R16 As authenticated user with a specific role I can access a document if its CLASSIFICATION is public and roles match"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['caseworker']

        givenRequest(CITIZEN_2, ['caseworker'])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R17 As authenticated user with a specific role I can access a document if its CLASSIFICATION is public and matches role"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['citizen', 'caseworker']

        givenRequest(CITIZEN_2, ['caseworker'])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R18 As authenticated user with no role I cannot access a document if its CLASSIFICATION is public with no role"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R19 As authenticated user with some role I cannot access a document if its CLASSIFICATION is public and roles does not match"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

    }

    @Test
    void "R20 As authenticated user with no role (Tests by default sets role as citizen) I can access a document if its CLASSIFICATION is public and roles is citizen"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'PUBLIC', ['citizen']

        givenRequest(CITIZEN_2, ['citizen'])
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R21 As an owner I can access a document even if its CLASSIFICATION is private with no roles"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN_2, ATTACHMENT_9_JPG, 'PRIVATE', [null]

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R22 As authenticated user with a specific role I can't access a document if its CLASSIFICATION is restricted and roles don't match"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN, ATTACHMENT_9_JPG, 'RESTRICTED', ['caseworker']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R23 As an Owner with no role I can access a document even if its CLASSIFICATION is private and role as caseworker"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN_2, ATTACHMENT_9_JPG, 'PRIVATE', ['caseworker']

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R24 I created a document using S2S token and only caseworkers should be able to read that using api gateway"() {

        def documentUrl = createDocumentAndGetUrlAs CITIZEN

        givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_PROBATE])
            .expect()
            .body("createdBy", equalTo(CITIZEN))
            .statusCode(200)
            .when()
            .get(documentUrl)

        givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_SSCS])
            .expect()
            .body("createdBy", equalTo(CITIZEN))
            .statusCode(200)
            .when()
            .get(documentUrl)

        givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_CMC])
            .expect()
            .body("createdBy", equalTo(CITIZEN))
            .statusCode(200)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R25 I created a document using S2S token, but I must not access it as a citizen using api gateway"() {

        def documentUrl = createDocumentAndGetBinaryUrlAs "user1"

        givenRequest(CITIZEN)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }


    @Test
    void "R26 userId provided during data creation can be obtained as username in the audit trail"() {

        def documentUrl = createDocumentAndGetUrlAs(CASE_WORKER)

        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(CASE_WORKER))
            .statusCode(200)
            .when()
            .get(documentUrl)


        String userNameFromResponse = givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_PROBATE])
            .when()
            .get(documentUrl + "/auditEntries")
            .body().path('_embedded.auditEntries[0].username');

        // TODO : This assert Fails on Nightly build only. Passes on Local,  Preview as well as Master build.
        //assertThat(userNameFromResponse,equalTo(CASE_WORKER));
    }

    @Test
    void "R27 As a citizen if I upload a document to API Store then I should be able to access it using API Gateway"() {

        def documentUrl = createDocumentAndGetBinaryUrlAs CITIZEN

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .when()
            .get(documentUrl)

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentUrl)
    }

    @Test
    void "R28 As authenticated user when get non existing document binary and see 404"() {

        String binaryUrl = createDocumentAndGetBinaryUrlAs CITIZEN
        def documentStr = "documents/"
        def nonExistentId = UUID.randomUUID().toString()
        binaryUrl = binaryUrl.replace(binaryUrl.substring(binaryUrl.indexOf(documentStr) + documentStr.length(), binaryUrl.lastIndexOf("/")), nonExistentId)

        givenRequest(CITIZEN)
            .expect()
            .statusCode(404)
            .when()
            .get(binaryUrl)
    }
}
