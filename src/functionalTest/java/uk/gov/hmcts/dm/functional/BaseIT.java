package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import net.jcip.annotations.NotThreadSafe;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.FunctionalTestContextConfiguration;
import uk.gov.hmcts.dm.StorageTestConfiguration;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.functional.DocumentMetadataPropertiesConfig.DocumentMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;



@NotThreadSafe
@ExtendWith(value = {SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest(classes = {FunctionalTestContextConfiguration.class, StorageTestConfiguration.class,
    ToggleConfiguration.class})
@WithTags(@WithTag("testType:Functional"))
public abstract class BaseIT {

    @Autowired
    private AuthTokenProvider authTokenProvider;
    @Autowired
    private DocumentMetadataPropertiesConfig metadataPropertiesConfig;
    @Autowired
    ToggleConfiguration toggleConfiguration;
    private FileUtils fileUtils = new FileUtils();
    @Value("${base-urls.dm-store}")
    private String dmStoreBaseUri;
    @Value("${base-urls.large-docs}")
    private String largeDocsBaseUri;
    @Value("${large-docs.metadata.mp4-111mb.id}")
    private String video111mbId;
    @Value("${large-docs.metadata.mp4-52mb.id}")
    private String video52mbId;
    @Value("${toggle.ttl}")
    private boolean toggleTtlEnabled;
    @Value("${toggle.metadatamigration}")
    private boolean metadataMigrationEnabled;

    @Value("${toggle.secureurl}")
    private boolean secureurl;

    private final String password = "123";
    private final String citizen = "test12@test.com";
    private String citizen2 = "test2@test.com";
    private String caseWorker = "test3@test.com";
    private String caseWorkerRoleProbate = "caseworker-probate";
    private String customUserRole = "custom-user-role";
    private String caseWorkerRoleCmc = "caseworker-cmc";
    private String caseWorkerRoleSscs = "caseworker-sscs";
    private String caseWorkerRoleDivorce = "caseworker-divorce";
    private final String nobody = null;
    private final String filesFolder = "files/";
    private final String textAttachment1 = "Attachment1.txt";
    private final String attachment2 = "Attachment2.txt";
    private final String attachment3 = "Attachment3.txt";
    private final String attachment4Pdf = "1MB.PDF";
    private final String attachment5 = "Attachment1.csv";
    private final String attachment6Gif = "marbles.gif";
    private final String attachment7Png = "png.png";
    private final String attachment8Tif = "tif.tif";
    private final String attachment9Jpg = "jpg.jpg";
    private final String attachment10 = "svg.svg";
    private final String attachment11 = "rtf.rtf";
    private final String attachment15 = "odt.odt";
    private final String attachment16 = "ods.ods";
    private final String attachment17 = "odp.odp";
    private final String attachment18 = "xml.xml";
    private final String attachment19 = "wav.wav";
    private final String attachment20 = "mid.mid";
    private final String attachment21 = "mp3.mp3";
    private final String attachment22 = "webm.webm";
    private final String attachment23 = "ogg.ogg";
    private final String attachment24 = "mp4.mp4";
    private final String attachment25Tiff = "tiff.tiff";
    private final String attachment26Bmp = "bmp.bmp";
    private final String attachment27Jpeg = "jpeg.jpeg";
    private final String word = "docx.docx";
    private final String wordMacroEnabledAsRegular = "docmHidden.docx";
    private final String powerPoint = "pptx.pptx";
    private final String excel = "xlsx.xlsx";
    private final String wordOld = "doc.doc";
    private final String excelOld = "xls.xls";
    private final String powerPointOld = "ppt.ppt";
    private final String wordTemplate = "dotx.dotx";
    private final String wordMacroEnabled = "docm.docm";
    private final String wordTemplateMacroEnabled = "dotm.dotm";
    private final String excelTemplate = "xltx.xltx";
    private final String excelMacroEnabled = "xlsm.xlsm";
    private final String excelTemplateMacroEnabled = "xltm.xltm";
    private final String powerPointMacroEnabled = "pptm.pptm";
    private final String powerPointTemplate = "potx.potx";
    private final String powerPointTemplateMacroEnabled = "potm.potm";
    private final String powerPointSlideShow = "ppsx.ppsx";
    private final String powerPointSlideShowMacroEnabled = "ppsm.ppsm";
    private final String badAttachment1 = "1MB.exe";
    private final String badAttachment2 = "Attachment3.zip";
    private final String illegalNameFile = "uploadFile~@$!.jpg";
    private final String illegalNameFile1 = "uploadFile~`';][{}!@£$%^&()}{_-.jpg";
    private final String illegalNameFile2 = "uploadFile9 @_-.jpg";
    private final String validCharFile1 = "uploadFile 9.txt";
    private final String exeAsPdf = "exe_as_pdf.pdf";
    private final String svgAsPdf = "svg_as_pdf.pdf";
    private final String xmlAsPdf = "xml_as_pdf.pdf";
    private final String exeAsPng = "exe_as_png.png";
    private final String svgAsPng = "svg_as_png.png";
    private final String xmlAsPng = "xml_as_png.png";
    private final String dropBoxUrl = "https://www.dropbox.com/s";

    @PostConstruct
    public void init() {
        SerenityRest.useRelaxedHTTPSValidation();
    }

    public RequestSpecification givenUnauthenticatedRequest() {
        return SerenityRest.given().baseUri(dmStoreBaseUri).log().all();
    }

    public RequestSpecification givenRequest(String username, List<String> userRoles) {

        RequestSpecification request = SerenityRest.given().baseUri(dmStoreBaseUri).log().all();
        if (username != null) {
            request = request.header("serviceauthorization", serviceToken());
            request = request.header("user-id", username);
        }
        if (userRoles != null) {
            request = request.header("user-roles", String.join(", ", userRoles));
        }

        return request;
    }

    public RequestSpecification givenRequest(String username) {
        return givenRequest(username, null);
    }

    public RequestSpecification givenRequest() {
        return givenRequest(null, null);
    }

    public RequestSpecification givenCcdCaseDisposerRequest() {

        RequestSpecification request = SerenityRest.given().baseUri(dmStoreBaseUri).log().all();
        request = request.header("serviceauthorization", authTokenProvider.findCcdCaseDisposerServiceToken());

        return request;
    }

    public RequestSpecification givenLargeFileRequest(String username, List<String> userRoles) {
        RequestSpecification request = SerenityRest.given().baseUri(largeDocsBaseUri).log().all();

        if (username != null) {
            request = request.header("serviceauthorization", serviceToken());
            request = request.header("user-id", username);
        }
        if (userRoles != null) {
            request = request.header("user-roles", String.join(", ", userRoles));
        }

        return request;
    }

    public RequestSpecification givenLargeFileRequest(String username) {
        return givenLargeFileRequest(username, null);
    }

    public RequestSpecification givenLargeFileRequest() {
        return givenLargeFileRequest(null, null);
    }

    public RequestSpecification givenSpacedRolesRequest(String username, List<String> userRoles) {

        RequestSpecification request = SerenityRest.given().baseUri(dmStoreBaseUri).log().all();
        if (username != null) {
            request = request.header("serviceauthorization", serviceToken());
            request = request.header("user-id", username);
        }
        if (userRoles != null) {
            request = request.header("user-roles", String.join(", ", userRoles));
        }

        return request;
    }

    public RequestSpecification givenSpacedRolesRequest(String username) {
        return givenSpacedRolesRequest(username, null);
    }

    public RequestSpecification givenSpacedRolesRequest() {
        return givenSpacedRolesRequest(null, null);
    }

    public RequestSpecification givenRangeRequest(long start, long end, String username, List<String> userRoles) {
        return givenRequest(username, userRoles).header("Range", "bytes=" + start + "-" + end);
    }

    public RequestSpecification givenRangeRequest(long start, long end, String username) {
        return givenRangeRequest(start, end, username, null);
    }

    public RequestSpecification givenRangeRequest(long start, long end) {
        return givenRangeRequest(start, end, null, null);
    }

    public RequestSpecification givenRangeRequest(long start) {
        return givenRangeRequest(start, 1023L, null, null);
    }

    public RequestSpecification givenRangeRequest() {
        return givenRangeRequest(0L, 1023L, null, null);
    }

    public RequestSpecification givenS2SRequest() {
        return SerenityRest.given().baseUri(dmStoreBaseUri)
            .log().all()
            .header("serviceauthorization", serviceToken())
            .header("cache-control", "no-cache");
    }

    public File file(Object fileName) {
        try {
            return fileUtils.getResourceFile(filesFolder + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public File largeFile(String doc, String metadataKey) throws IOException {


        DocumentMetadata metadata = metadataPropertiesConfig.getMetadata().get(metadataKey);
        String name = metadata.getDocumentName();
        String extension = metadata.getExtension();
        File tmpFile = File.createTempFile(name, "." + extension);

        try (
            OutputStream outputStream = new FileOutputStream(tmpFile);

            final InputStream inputStream = givenLargeFileRequest(citizen,
                new ArrayList<>(List.of(caseWorkerRoleProbate)))
                .get(doc)
                .getBody()
                .asInputStream()) {
            outputStream.write(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tmpFile;
    }

    public String authToken(Object username) {
        return authTokenProvider.getTokens((String) username, password).getUserToken();
    }

    public String userId(String token) {
        return String.valueOf(authTokenProvider.findUserId(token));
    }

    public String serviceToken() {
        return authTokenProvider.findServiceToken();
    }

    public String ccdCaseDisposerServiceToken() {
        return authTokenProvider.findCcdCaseDisposerServiceToken();
    }

    public Response createDocument(String username, String filename, String classification, List<String> roles,
                                   Map<String, String> metadata) {
        RequestSpecification request = givenRequest(username)
            .multiPart("files", file(filename != null ? filename : attachment9Jpg), MediaType.IMAGE_JPEG_VALUE)
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

    public Response createDocument(String username) {
        return createDocument(username, null, null, Collections.emptyList(), null);
    }

    public String createDocumentAndGetUrlAs(String username, String filename, String classification,
                                            List<String> roles, Map<String, String> metadata) {
        String documentUrl =  createDocument(username, filename, classification, roles, metadata)
            .path("_embedded.documents[0]._links.self.href");
        return replaceHttp(documentUrl);
    }

    public String createDocumentAndGetUrlAs(String username, String filename,
                                            String classification, List<String> roles) {
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

    public Response fetchDocumentMetaDataAs(String username, String documentUrl) {
        return givenRequest(username).get(documentUrl);
    }

    public String createDocumentAndGetBinaryUrlAs(String username, String filename,
                                                  String classification, List<String> roles) {
        String url = createDocument(username, filename, classification, roles)
            .path("_embedded.documents[0]._links.binary.href");
        return replaceHttp(url);
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
        return givenRequest(username)
            .multiPart("file", file(filename != null ? filename : attachment9Jpg), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(201)
            .when()
            .post(documentUrl);
    }

    public Response createDocumentContentVersion(String documentUrl, String username) {
        return createDocumentContentVersion(documentUrl, username, null);
    }

    public String createDocumentContentVersionAndGetUrlAs(String documentUrl, String username, String filename) {
        return replaceHttp(createDocumentContentVersion(documentUrl, username, filename).path("_links.self.href"));
    }

    public String createDocumentContentVersionAndGetUrlAs(String documentUrl, String username) {
        return createDocumentContentVersionAndGetUrlAs(documentUrl, username, null);
    }

    public String createDocumentContentVersionAndGetBinaryUrlAs(String documentUrl, String username, String filename) {
        return replaceHttp(createDocumentContentVersion(documentUrl, username, filename).path("_links.binary.href"));
    }

    public String createDocumentContentVersionAndGetBinaryUrlAs(String documentUrl, String username) {
        return createDocumentContentVersionAndGetBinaryUrlAs(documentUrl, username, null);
    }

    public Response createAUserForTtl(String username) {
        return givenRequest(username)
            .multiPart("files", file(attachment9Jpg), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all().statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(attachment9Jpg))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", Matchers.equalTo("caseworker"))
            .body("_embedded.documents[0].ttl", Matchers.equalTo("2018-10-31T10:10:10+0000"))
            .when().post("/documents");
    }

    protected String replaceHttp(String url) {
        if (secureurl) {
            return url.replace("http","https");
        }
        return url;
    }

    public void assertByteArrayEquality(String fileName, byte[] response) throws IOException {
        Assert.assertArrayEquals(Files.readAllBytes(file(fileName).toPath()), response);
    }

    public void assertLargeDocByteArrayEquality(File file, byte[] response) throws IOException {
        Assert.assertArrayEquals(Files.readAllBytes(file.toPath()), response);
    }

    public AuthTokenProvider getAuthTokenProvider() {
        return authTokenProvider;
    }

    public void setAuthTokenProvider(AuthTokenProvider authTokenProvider) {
        this.authTokenProvider = authTokenProvider;
    }

    public DocumentMetadataPropertiesConfig getMetadataPropertiesConfig() {
        return metadataPropertiesConfig;
    }

    public void setMetadataPropertiesConfig(DocumentMetadataPropertiesConfig metadataPropertiesConfig) {
        this.metadataPropertiesConfig = metadataPropertiesConfig;
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

    public String getLargeDocsBaseUri() {
        return largeDocsBaseUri;
    }

    public void setLargeDocsBaseUri(String largeDocsBaseUri) {
        this.largeDocsBaseUri = largeDocsBaseUri;
    }

    public String getVideo111mbId() {
        return video111mbId;
    }

    public void setVideo111mbId(String video111mbId) {
        this.video111mbId = video111mbId;
    }

    public String getVideo52mbId() {
        return video52mbId;
    }

    public void setVideo52mbId(String video52mbId) {
        this.video52mbId = video52mbId;
    }

    public boolean getToggleTtlEnabled() {
        return toggleTtlEnabled;
    }

    public boolean isToggleTtlEnabled() {
        return toggleTtlEnabled;
    }

    public void setToggleTtlEnabled(boolean toggleTtlEnabled) {
        this.toggleTtlEnabled = toggleTtlEnabled;
    }

    public boolean getMetadataMigrationEnabled() {
        return metadataMigrationEnabled;
    }

    public boolean isMetadataMigrationEnabled() {
        return metadataMigrationEnabled;
    }

    public void setMetadataMigrationEnabled(boolean metadataMigrationEnabled) {
        this.metadataMigrationEnabled = metadataMigrationEnabled;
    }

    public String getCitizen() {
        return citizen;
    }

    public String getCitizen2() {
        return citizen2;
    }

    public void setCitizen2(String citizen2) {
        this.citizen2 = citizen2;
    }

    public String getCaseWorker() {
        return caseWorker;
    }

    public void setCaseWorker(String caseWorker) {
        this.caseWorker = caseWorker;
    }

    public String getCaseWorkerRoleProbate() {
        return caseWorkerRoleProbate;
    }

    public void setCaseWorkerRoleProbate(String caseWorkerRoleProbate) {
        this.caseWorkerRoleProbate = caseWorkerRoleProbate;
    }

    public String getCustomUserRole() {
        return customUserRole;
    }

    public void setCustomUserRole(String customUserRole) {
        this.customUserRole = customUserRole;
    }

    public String getCaseWorkerRoleCmc() {
        return caseWorkerRoleCmc;
    }

    public void setCaseWorkerRoleCmc(String caseWorkerRoleCmc) {
        this.caseWorkerRoleCmc = caseWorkerRoleCmc;
    }

    public String getCaseWorkerRoleSscs() {
        return caseWorkerRoleSscs;
    }

    public void setCaseWorkerRoleSscs(String caseWorkerRoleSscs) {
        this.caseWorkerRoleSscs = caseWorkerRoleSscs;
    }

    public String getCaseWorkerRoleDivorce() {
        return caseWorkerRoleDivorce;
    }

    public void setCaseWorkerRoleDivorce(String caseWorkerRoleDivorce) {
        this.caseWorkerRoleDivorce = caseWorkerRoleDivorce;
    }

    public final String getNobody() {
        return nobody;
    }

    public final String getFilesFolder() {
        return filesFolder;
    }

    public final String getTextAttachment1() {
        return textAttachment1;
    }

    public final String getAttachment2() {
        return attachment2;
    }

    public final String getAttachment3() {
        return attachment3;
    }

    public final String getAttachment4Pdf() {
        return attachment4Pdf;
    }

    public final String getAttachment5() {
        return attachment5;
    }

    public final String getAttachment6Gif() {
        return attachment6Gif;
    }

    public final String getAttachment7Png() {
        return attachment7Png;
    }

    public final String getAttachment8Tif() {
        return attachment8Tif;
    }

    public final String getAttachment9Jpg() {
        return attachment9Jpg;
    }

    public final String getAttachment10() {
        return attachment10;
    }

    public final String getAttachment11() {
        return attachment11;
    }

    public final String getAttachment15() {
        return attachment15;
    }

    public final String getAttachment16() {
        return attachment16;
    }

    public final String getAttachment17() {
        return attachment17;
    }

    public final String getAttachment18() {
        return attachment18;
    }

    public final String getAttachment19() {
        return attachment19;
    }

    public final String getAttachment20() {
        return attachment20;
    }

    public final String getAttachment21() {
        return attachment21;
    }

    public final String getAttachment22() {
        return attachment22;
    }

    public final String getAttachment23() {
        return attachment23;
    }

    public final String getAttachment24() {
        return attachment24;
    }

    public final String getAttachment25Tiff() {
        return attachment25Tiff;
    }

    public final String getAttachment26Bmp() {
        return attachment26Bmp;
    }

    public final String getAttachment27Jpeg() {
        return attachment27Jpeg;
    }

    public final String getWord() {
        return word;
    }

    public final String getWordMacroEnabledAsRegular() {
        return wordMacroEnabledAsRegular;
    }

    public final String getPowerPoint() {
        return powerPoint;
    }

    public final String getExcel() {
        return excel;
    }

    public final String getWordOld() {
        return wordOld;
    }

    public final String getExcelOld() {
        return excelOld;
    }

    public final String getPowerPointOld() {
        return powerPointOld;
    }

    public final String getWordTemplate() {
        return wordTemplate;
    }

    public final String getWordMacroEnabled() {
        return wordMacroEnabled;
    }

    public final String getWordTemplateMacroEnabled() {
        return wordTemplateMacroEnabled;
    }

    public final String getExcelTemplate() {
        return excelTemplate;
    }

    public final String getExcelMacroEnabled() {
        return excelMacroEnabled;
    }

    public final String getExcelTemplateMacroEnabled() {
        return excelTemplateMacroEnabled;
    }

    public final String getPowerPointMacroEnabled() {
        return powerPointMacroEnabled;
    }

    public final String getPowerPointTemplate() {
        return powerPointTemplate;
    }

    public final String getPowerPointTemplateMacroEnabled() {
        return powerPointTemplateMacroEnabled;
    }

    public final String getPowerPointSlideShow() {
        return powerPointSlideShow;
    }

    public final String getPowerPointSlideShowMacroEnabled() {
        return powerPointSlideShowMacroEnabled;
    }

    public final String getBadAttachment1() {
        return badAttachment1;
    }

    public final String getBadAttachment2() {
        return badAttachment2;
    }

    public final String getIllegalNameFile() {
        return illegalNameFile;
    }

    public final String getIllegalNameFile1() {
        return illegalNameFile1;
    }

    public final String getIllegalNameFile2() {
        return illegalNameFile2;
    }

    public final String getValidCharFile1() {
        return validCharFile1;
    }

    public final String getExeAsPdf() {
        return exeAsPdf;
    }

    public final String getSvgAsPdf() {
        return svgAsPdf;
    }

    public final String getXmlAsPdf() {
        return xmlAsPdf;
    }

    public final String getExeAsPng() {
        return exeAsPng;
    }

    public final String getSvgAsPng() {
        return svgAsPng;
    }

    public final String getXmlAsPng() {
        return xmlAsPng;
    }

    public boolean isDropBoxFile() {
        return dropBoxUrl.equals(largeDocsBaseUri);
    }
}
