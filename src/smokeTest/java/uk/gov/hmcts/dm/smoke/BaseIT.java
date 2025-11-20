package uk.gov.hmcts.dm.smoke;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import net.jcip.annotations.NotThreadSafe;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.dm.smoke.utilities.FileUtils;
import uk.gov.hmcts.dm.smoke.utilities.V1MediaTypes;

import java.io.File;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes = uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration.class)
@NotThreadSafe
@WithTags(@WithTag("testType:Smoke"))
public class BaseIT {


    private final AuthTokenProvider authTokenProvider;
    private final FileUtils fileUtils;
    @Value("${base-urls.dm-store}")
    private String dmStoreBaseUri;
    private static final String FILES_FOLDER = "files/";
    private static final String ATTACHMENT_1 = "Attachment1.txt";

    @Autowired
    public BaseIT(AuthTokenProvider authTokenProvider) {
        this.authTokenProvider = authTokenProvider;
        this.fileUtils =  new FileUtils();
    }


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
        return fileUtils.getResourceFile(FILES_FOLDER + fileName);
    }

    public Response createDocument(String filename, String classification, List<String> roles,
                                   Map<String, String> metadata) {
        RequestSpecification request = givenRequest()
            .multiPart("files", file(filename != null ? filename : ATTACHMENT_1), MediaType.TEXT_PLAIN_VALUE)
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

    public Response createDocumentContentVersion(String documentUrl, String filename) {
        return givenRequest()
            .multiPart("file", file(filename != null ? filename : ATTACHMENT_1), MediaType.TEXT_PLAIN_VALUE)
            .expect()
            .statusCode(201)
            .when()
            .post(documentUrl);
    }

    public AuthTokenProvider getAuthTokenProvider() {
        return authTokenProvider;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public String getDmStoreBaseUri() {
        return dmStoreBaseUri;
    }

    public void setDmStoreBaseUri(String dmStoreBaseUri) {
        this.dmStoreBaseUri = dmStoreBaseUri;
    }


}
