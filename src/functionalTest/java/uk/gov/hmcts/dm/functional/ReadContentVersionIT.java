package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ReadContentVersionIT extends BaseIT {

    private String documentUrl;
    private Object documentVersion;
    private String documentVersionUrl;
    private String documentVersionBinaryUrl;

    @BeforeEach
    public void setup() {
        documentUrl = createDocumentAndGetUrlAs(getCitizen());
        documentVersion = createDocumentContentVersion(documentUrl, getCitizen(), getAttachment9Jpg());
        documentVersionUrl = replaceHttp(((Response) documentVersion).path("_links.self.href"));
        documentVersionBinaryUrl = replaceHttp(((Response) documentVersion).path("_links.binary.href"));
    }

    @Test
    public void rcv1AsCreatorIReadContentVersionByUrl() {

        givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE_VALUE)
            .body("originalDocumentName", equalTo(getAttachment9Jpg()))
            .body("mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .when()
            .get(documentVersionUrl);
    }

    @Test
    public void rcv2AsCreatorIReadContentVersionBinaryByUrl() throws IOException {

        assertByteArrayEquality(getAttachment9Jpg(), givenRequest(getCitizen())
            .expect()
            .statusCode(200)
            .contentType(MediaType.IMAGE_JPEG_VALUE)
            .when()
            .get(documentVersionBinaryUrl)
            .asByteArray());
    }

    @Test
    public void rcv3AsNotOwnerAndNotACaseWorkerIReadContentVersionByUrlButIAmDeniedAccess() {

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionUrl);
    }

    @Test
    public void rcv4AsNotOwnerAndNotACaseWorkerIReadContentVersionBinaryByUrlButIAmDeniedAccess() {

        givenRequest(getCitizen2())
            .expect()
            .statusCode(403)
            .when()
            .get(documentVersionBinaryUrl);
    }

    @Test
    public void rcv6AsAProbateCaseWorkerIReadContentVersionBinaryByUrl() throws IOException {

        assertByteArrayEquality(getAttachment9Jpg(),
            givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleProbate())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());

    }

    @Test
    public void rcv7AsACmcCaseWorkerICanReadContentVersionBinaryByUrl() throws IOException {

        assertByteArrayEquality(getAttachment9Jpg(),
            givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleCmc())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void rcv8AsASscsCaseWorkerICanReadContentVersionBinaryByUrl() throws IOException {

        assertByteArrayEquality(getAttachment9Jpg(),
            givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleSscs())))
                .expect()
                .statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void rcv9AsADivorceCaseWorkerICanReadContentVersionBinaryByUrl() throws IOException {

        assertByteArrayEquality(getAttachment9Jpg(),
            givenRequest(getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleDivorce())))
                .expect().statusCode(200)
                .when()
                .get(documentVersionBinaryUrl)
                .asByteArray());
    }

    @Test
    public void rcv10AsACreatorWhenIReadNonExistentVersionByUrl() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        final String newDocumentVersionUrl = documentVersionUrl.replace(
            documentVersionUrl.substring(documentVersionUrl.lastIndexOf("/") + 1), nonExistentVersionId);

        givenRequest(getCitizen())
            .when()
            .get(newDocumentVersionUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found",
                nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all();
    }

    @Test
    public void rcv11AsACreatorWhenIReadNonExistentVersionBinaryByUrl() {
        final String nonExistentVersionId = UUID.randomUUID().toString();
        String versionsStr = "versions";
        String url = documentVersionBinaryUrl.substring(0, (
            documentVersionBinaryUrl.indexOf(versionsStr) + versionsStr.length()));
        String binaryPath = "/%s/binary";
        String newDocumentVersionBinaryUrl = url + String.format(binaryPath, nonExistentVersionId);

        givenRequest(getCitizen())
            .when()
            .get(newDocumentVersionBinaryUrl)
            .then()
            .assertThat()
            .statusCode(is(404))
            .body("error", equalTo(String.format("DocumentContentVersion with ID: %s could not be found",
                nonExistentVersionId)))
            .body("exception", equalTo("uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException"))
            .log()
            .all();
    }

    @Test
    public void rcv12AsADivorceCaseWorkerICanReadContentVersionBinaryByUrlUsingHttpRangeHeaders() {
        givenRangeRequest(0L, 99L, getCaseWorker(), new ArrayList<>(List.of(getCaseWorkerRoleDivorce())))
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
