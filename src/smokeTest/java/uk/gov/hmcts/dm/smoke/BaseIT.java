package uk.gov.hmcts.dm.smoke;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.jcip.annotations.NotThreadSafe;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.dm.smoke.utilities.FileUtils;
import uk.gov.hmcts.dm.smoke.utilities.V1MediaTypes;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes = uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration.class)
@NotThreadSafe
@WithTags(@WithTag("testType:Smoke"))
public class BaseIT {

    @Autowired
    private AuthTokenProvider authTokenProvider;
    private FileUtils fileUtils = new FileUtils();
    @Value("${base-urls.dm-store}")
    private String dmStoreBaseUri;
    private final String filesFolder = "files/";
    private final String attachment1 = "Attachment1.txt";

    @PostConstruct
    public void init() {
        SerenityRest.useRelaxedHTTPSValidation();
    }

    public RequestSpecification givenUnauthenticatedRequest() {
        return SerenityRest.given().baseUri(dmStoreBaseUri).log().all();
    }

    public RequestSpecification givenRequest() {
        return SerenityRest.given().baseUri(dmStoreBaseUri).log().all().header("ServiceAuthorization", serviceToken());
    }

    public String serviceToken() {
        return authTokenProvider.findServiceToken();
    }

    public File file(String fileName) {
        return fileUtils.getResourceFile(filesFolder + fileName);
    }

    public Response createDocument(String username, String filename, String classification, List<String> roles,
                                   Map<String, String> metadata) {
        RequestSpecification request = givenRequest()
            .multiPart("files", file(filename != null ? filename : attachment1), MediaType.TEXT_PLAIN_VALUE)
            .multiPart("classification", classification != null ? classification : "PUBLIC");

        for (String role : roles) {
            request.multiPart("roles", role);
        }

        if (metadata != null) {
            request.accept(V1MediaTypes.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE_VALUE);
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                request.multiPart("metadata[" + entry.getKey() + "]", entry.getValue());
            }
        }
        return request.expect().statusCode(200).when().post("/documents");
    }

    public Response createDocument(String username, String filename, String classification, List<String> roles) {
        return createDocument(username, filename, classification, roles, null);
    }

    public Response createDocument(String username, String filename, String classification) {
        return createDocument(username, filename, classification, Collections.emptyList(), null);
    }

    public Response createDocument(String username, String filename) {
        return createDocument(username, filename, null, Collections.emptyList(), null);
    }

    public String createDocumentAndGetUrlAs(String username, String filename, String classification, List<String> roles, Map<String, String> metadata) {
        return createDocument(username, filename, classification, roles, metadata).path("_embedded.documents[0]._links.self.href");
    }

    public String createDocumentAndGetUrlAs(String username, String filename, String classification, List<String> roles) {
        return createDocumentAndGetUrlAs(username, filename, classification, roles, null);
    }

    public String createDocumentAndGetUrlAs(String username, String filename, String classification) {
        return createDocumentAndGetUrlAs(username, filename, classification, Collections.emptyList(), null);
    }

    public String createDocumentAndGetUrlAs(String username, String filename) {
        return createDocumentAndGetUrlAs(username, filename, null, Collections.emptyList(), null);
    }

    public String createDocumentAndGetUrlAs(String username) {
        return createDocumentAndGetUrlAs(username, null, null, Collections.emptyList(), null);
    }

    public String createDocumentAndGetBinaryUrlAs(String username, String filename, String classification, List<String> roles) {
        return createDocument(username, filename, classification, roles).path("_embedded.documents[0]._links.binary.href");
    }

    public String createDocumentAndGetBinaryUrlAs(String username, String filename, String classification) {
        return createDocumentAndGetBinaryUrlAs(username, filename, classification, Collections.emptyList());
    }

    public String createDocumentAndGetBinaryUrlAs(String username, String filename) {
        return createDocumentAndGetBinaryUrlAs(username, filename, null, Collections.emptyList());
    }

    public String createDocumentAndGetBinaryUrlAs(String username) {
        return createDocumentAndGetBinaryUrlAs(username, null, null, Collections.emptyList());
    }

    public Response createDocumentContentVersion(String documentUrl, String username, String filename) {
        return givenRequest()
            .multiPart("file", file(filename != null ? filename : attachment1), MediaType.TEXT_PLAIN_VALUE)
            .expect()
            .statusCode(201)
            .when()
            .post(documentUrl);
    }

    public Response createDocumentContentVersion(String documentUrl, String username) {
        return createDocumentContentVersion(documentUrl, username, null);
    }

    public String createDocumentContentVersionAndGetUrlAs(String documentUrl, String username, String filename) {
        return createDocumentContentVersion(documentUrl, username, filename).path("_links.self.href");
    }

    public String createDocumentContentVersionAndGetUrlAs(String documentUrl, String username) {
        return createDocumentContentVersionAndGetUrlAs(documentUrl, username, null);
    }

    public String createDocumentContentVersionAndGetBinaryUrlAs(String documentUrl, String username, String filename) {
        return createDocumentContentVersion(documentUrl, username, filename).path("_links.binary.href");
    }

    public String createDocumentContentVersionAndGetBinaryUrlAs(String documentUrl, String username) {
        return createDocumentContentVersionAndGetBinaryUrlAs(documentUrl, username, null);
    }

    public AuthTokenProvider getAuthTokenProvider() {
        return authTokenProvider;
    }

    public void setAuthTokenProvider(AuthTokenProvider authTokenProvider) {
        this.authTokenProvider = authTokenProvider;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public void setFileUtils(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public String getDmStoreBaseUri() {
        return dmStoreBaseUri;
    }

    public void setDmStoreBaseUri(String dmStoreBaseUri) {
        this.dmStoreBaseUri = dmStoreBaseUri;
    }

    public final String getFilesFolder() {
        return filesFolder;
    }

    public final String getAttachment1() {
        return attachment1;
    }


}
