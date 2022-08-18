package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ReadContentVersionIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    private String documentUrl;
    private Object documentVersion;
    private String documentVersionUrl;
    private String documentVersionBinaryUrl;

    @Before
    public void setup() {
        documentUrl = createDocumentAndGetUrlAs(getCITIZEN());
        documentVersion = createDocumentContentVersion(documentUrl, getCITIZEN(), getATTACHMENT_9_JPG());
        documentVersionUrl = ((Response) documentVersion).path("_links.self.href");
        documentVersionBinaryUrl = ((Response) documentVersion).path("_links.binary.href");
    }

    @Test
    public void RCV1_As_creator_I_read_content_version_by_URL() {

        givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getATTACHMENT_9_JPG()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .get(documentVersionUrl);
    }

    @Test
    public void RCV2_As_creator_I_read_content_version_binary_by_URL() throws IOException {

        assertByteArrayEquality (getATTACHMENT_9_JPG(), givenRequest(getCITIZEN())
            .expect()
            .statusCode(200)
            .contentType(MediaType.IMAGE_JPEG_VALUE)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray());
    }

    @Test
    public void RCV3_As_not_owner_and_not_a_case_worker_I_read_content_version_by_URL_but_I_am_denied_access() {

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionUrl);
    }

    @Test
    public void RCV4_As_not_owner_and_not_a_case_worker_I_read_content_version_binary_by_URL_but_I_am_denied_access() {

        givenRequest(getCITIZEN_2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionBinaryUrl);
    }

    @Test
    public void RCV6_As_a_probate_case_worker_I_read_content_version_binary_by_URL() throws IOException {

        assertByteArrayEquality(getATTACHMENT_9_JPG(),
            givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_PROBATE())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());

    }

    @Test
    public void RCV7_As_a_cmc_case_worker_I_can_read_content_version_binary_by_URL() throws IOException {

        assertByteArrayEquality(getATTACHMENT_9_JPG(),
            givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_CMC())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void RCV8_As_a_sscs_case_worker_I_can_read_content_version_binary_by_URL() throws IOException {

        assertByteArrayEquality(getATTACHMENT_9_JPG(),
            givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_SSCS())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void RCV9_As_a_divorce_case_worker_I_can_read_content_version_binary_by_URL() throws IOException {

        assertByteArrayEquality(getATTACHMENT_9_JPG(),
            givenRequest(getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_DIVORCE())))
                .expect().statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void RCV10_As_a_creator_when_i_read_non_existent_version_by_URL() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        final String newDocumentVersionUrl = documentVersionUrl.replace(documentVersionUrl.substring(documentVersionUrl.lastIndexOf("/") + 1), nonExistentVersionId);

        givenRequest(getCITIZEN())
            .when()
            .get(newDocumentVersionUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found", nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all();
    }

    @Test
    public void RCV11_As_a_creator_when_i_read_non_existent_version_binary_by_URL() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        String versionsStr = "versions";
        String url = documentVersionBinaryUrl.substring(0, (documentVersionBinaryUrl.indexOf(versionsStr) + versionsStr.length()));
        String binaryPath = "/%s/binary";
        String newDocumentVersionBinaryUrl = url + String.format(binaryPath, nonExistentVersionId);

        givenRequest(getCITIZEN())
            .when()
            .get(newDocumentVersionBinaryUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found", nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all();
    }

    @Test
    public void RCV12_As_a_divorce_case_worker_I_can_read_content_version_binary_by_URL_using_HTTP_Range_Headers() {
        givenRangeRequest(0L, 99L, getCASE_WORKER(), new ArrayList<>(List.of(getCASE_WORKER_ROLE_DIVORCE())))
            .expect()
            .statusCode(206)
            .header(HttpHeaders.CONTENT_LENGTH, "100")
            .header(HttpHeaders.RANGE, "0-99/45972");
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public Object getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(Object documentVersion) {
        this.documentVersion = documentVersion;
    }

    public String getDocumentVersionUrl() {
        return documentVersionUrl;
    }

    public void setDocumentVersionUrl(String documentVersionUrl) {
        this.documentVersionUrl = documentVersionUrl;
    }

    public String getDocumentVersionBinaryUrl() {
        return documentVersionBinaryUrl;
    }

    public void setDocumentVersionBinaryUrl(String documentVersionBinaryUrl) {
        this.documentVersionBinaryUrl = documentVersionBinaryUrl;
    }

}
