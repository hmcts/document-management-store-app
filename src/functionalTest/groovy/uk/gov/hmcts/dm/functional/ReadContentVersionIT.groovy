package uk.gov.hmcts.dm.functional

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is

class ReadContentVersionIT extends BaseIT {

    String documentUrl

    def documentVersion

    String documentVersionUrl

    String documentVersionBinaryUrl

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Before
    public void setup() throws Exception {
        documentUrl = createDocumentAndGetUrlAs CITIZEN
        documentVersion = createDocumentContentVersion documentUrl, CITIZEN, ATTACHMENT_9_JPG
        documentVersionUrl = documentVersion.path('_links.self.href')
        documentVersionBinaryUrl = documentVersion.path('_links.binary.href')
    }

    @Test
    void "RCV1 As creator I read content version by URL"() {

        givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .get(documentVersionUrl)

    }


    @Test
    void "RCV2 As creator I read content version binary by URL"() {

        assertByteArrayEquality ATTACHMENT_9_JPG, givenRequest(CITIZEN)
            .expect()
            .statusCode(200)
            .contentType(MediaType.IMAGE_JPEG_VALUE)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray()

    }

    @Test
    void "RCV3 As not owner and not a case worker I read content version by URL but I am denied access"() {

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionUrl)

    }

    @Test
    void "RCV4 As not owner and not a case worker I read content version binary by URL but I am denied access"() {

        givenRequest(CITIZEN_2)
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionBinaryUrl)

    }

    @Test
    void "RCV6 As a probate case-worker I read content version binary by URL"() {

        assertByteArrayEquality ATTACHMENT_9_JPG, givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_PROBATE])
            .expect()
            .statusCode(200)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray()

    }

    @Test
    void "RCV7 As a cmc case-worker I can read content version binary by URL"() {

        assertByteArrayEquality ATTACHMENT_9_JPG, givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_CMC])
            .expect()
            .statusCode(200)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray()
    }

    @Test
    void "RCV8 As a sscs case-worker I can read content version binary by URL"() {

        assertByteArrayEquality ATTACHMENT_9_JPG, givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_SSCS])
            .expect()
            .statusCode(200)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray()
    }

    @Test
    void "RCV9 As a divorce case-worker I can read content version binary by URL"() {

        assertByteArrayEquality ATTACHMENT_9_JPG, givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_DIVORCE])
            .expect()
            .statusCode(200)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray()
    }

    @Test
    void "RCV10 As a creator when i read non existent version by URL"() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        final String newDocumentVersionUrl = documentVersionUrl.replace(documentVersionUrl.substring(documentVersionUrl.lastIndexOf("/") + 1), nonExistentVersionId);

        givenRequest(CITIZEN)
            .when()
            .get(newDocumentVersionUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found", nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all()
    }

    @Test
    void "RCV11 As a creator when i read non existent version binary by URL"() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        def versionsStr = "versions"
        def url = documentVersionBinaryUrl.substring(0, (documentVersionBinaryUrl.indexOf(versionsStr) + versionsStr.length()))
        def binaryPath = "/%s/binary"
        def newDocumentVersionBinaryUrl = url + String.format(binaryPath, nonExistentVersionId)

        givenRequest(CITIZEN)
            .when()
            .get(newDocumentVersionBinaryUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found", nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all()
    }

    @Test
    void "RCV12 As a divorce case-worker I can read content version binary by URL using HTTP Range Headers"() {
        givenRangeRequest(0L, 99L, CASE_WORKER, [CASE_WORKER_ROLE_DIVORCE])
            .expect()
            .statusCode(206)
            .header(HttpHeaders.CONTENT_LENGTH, "100")
            .header(HttpHeaders.RANGE, "0-99/45972")
    }

}
