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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@SuppressWarnings({"java:S6813", "java:S5960"}) // Suppress SonarQube warning for autowired field and assertions
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

    private static final String USER_ID_CONST =  "user-id";
    private static final String USER_ROLES_CONST =  "user-roles";
    private static final String ROLES_CONST =  "roles";
    private static final String PASSWORD = "123";
    private static final String CITIZEN = "test12@test.com";
    private static final String CITIZEN_2 = "test2@test.com";
    private static final String CASE_WORKER = "test3@test.com";
    private static final String CASE_WORKER_ROLE_PROBATE = "caseworker-probate";
    private static final String CUSTOM_USER_ROLE = "custom-user-role";
    private static final String CASE_WORKER_ROLE_CMC = "caseworker-cmc";
    private static final String CASE_WORKER_ROLE_SSCS = "caseworker-sscs";
    private static final String CASE_WORKER_ROLE_DIVORCE = "caseworker-divorce";
    private static final String NO_BODY = null;
    private static final String FILES_FOLDER = "files/";
    private static final String TEXT_ATTACHMENT_1 = "Attachment1.txt";
    private static final String ATTACHMENT_2 = "Attachment2.txt";
    private static final String ATTACHMENT_3 = "Attachment3.txt";
    private static final String ATTACHMENT_4_PDF = "1MB.PDF";
    private static final String ATTACHMENT_5 = "Attachment1.csv";
    private static final String ATTACHMENT_6_GIF = "marbles.gif";
    private static final String ATTACHMENT_7_PNG = "png.png";
    private static final String ATTACHMENT_8_TIF = "tif.tif";
    private static final String ATTACHMENT_9_JPG = "jpg.jpg";
    private static final String ATTACHMENT_10 = "svg.svg";
    private static final String ATTACHMENT_11 = "rtf.rtf";
    private static final String ATTACHMENT_15 = "odt.odt";
    private static final String ATTACHMENT_16 = "ods.ods";
    private static final String ATTACHMENT_17 = "odp.odp";
    private static final String ATTACHMENT_18 = "xml.xml";
    private static final String ATTACHMENT_19 = "wav.wav";
    private static final String ATTACHMENT_20 = "mid.mid";
    private static final String ATTACHMENT_21 = "mp3.mp3";
    private static final String ATTACHMENT_22 = "webm.webm";
    private static final String ATTACHMENT_23 = "ogg.ogg";
    private static final String ATTACHMENT_24 = "mp4.mp4";
    private static final String ATTACHMENT_25_TIFF = "tiff.tiff";
    private static final String ATTACHMENT_26_BMP = "bmp.bmp";
    private static final String ATTACHMENT_27_JPEG = "jpeg.jpeg";
    private static final String WORD = "docx.docx";
    private static final String WORD_MACRO_ENABLED_AS_REGULAR = "docmHidden.docx";
    private static final String POWER_POINT = "pptx.pptx";
    private static final String EXCEL = "xlsx.xlsx";
    private static final String WORD_OLD = "doc.doc";
    private static final String EXCEL_OLD = "xls.xls";
    private static final String POWER_POINT_OLD = "ppt.ppt";
    private static final String WORD_TEMPLATE = "dotx.dotx";
    private static final String WORD_MACRO_ENABLED = "docm.docm";
    private static final String WORD_TEMPLATE_MACRO_ENABLED = "dotm.dotm";
    private static final String EXCEL_TEMPLATE = "xltx.xltx";
    private static final String EXCEL_MACRO_ENABLED = "xlsm.xlsm";
    private static final String EXCEL_TEMPLATE_MACRO_ENABLED = "xltm.xltm";
    private static final String POWER_POINT_MACRO_ENABLED = "pptm.pptm";
    private static final String POWER_POINT_TEMPLATE = "potx.potx";
    private static final String POWER_POINT_TEMPLATE_MACRO_ENABLED = "potm.potm";
    private static final String POWER_POINT_SLIDE_SHOW = "ppsx.ppsx";
    private static final String POWER_POINT_SLIDE_SHOW_MACRO_ENABLED = "ppsm.ppsm";
    private static final String BAD_ATTACHMENT_1 = "1MB.exe";
    private static final String BAD_ATTACHMENT_2 = "Attachment3.zip";
    private static final String ILLEGAL_NAME_FILE = "uploadFile~@$!.jpg";
    private static final String ILLEGAL_NAME_FILE_1 = "uploadFile~`';][{}!@£$%^&()}{_-.jpg";
    private static final String ILLEGAL_NAME_FILE_2 = "uploadFile9 @_-.jpg";
    private static final String VALID_CHAR_FILE_1 = "uploadFile 9.txt";
    private static final String EXE_AS_PDF = "exe_as_pdf.pdf";
    private static final String SVG_AS_PDF = "svg_as_pdf.pdf";
    private static final String XML_AS_PDF = "xml_as_pdf.pdf";
    private static final String EXE_AS_PNG = "exe_as_png.png";
    private static final String SVG_AS_PNG = "svg_as_png.png";
    private static final String XML_AS_PNG = "xml_as_png.png";
    private static final String DROP_BOX_URL = "https://www.dropbox.com/s";
    private static final String SERVICE_AUTHORIZATION_HEADER = "serviceauthorization";

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
            request = request.header(SERVICE_AUTHORIZATION_HEADER, serviceToken());
            request = request.header(USER_ID_CONST, username);
        }
        if (userRoles != null) {
            request = request.header(USER_ROLES_CONST, String.join(", ", userRoles));
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
        request = request.header(SERVICE_AUTHORIZATION_HEADER, authTokenProvider.findCcdCaseDisposerServiceToken());

        return request;
    }

    public RequestSpecification givenLargeFileRequest(String username, List<String> userRoles) {
        RequestSpecification request = SerenityRest.given().baseUri(largeDocsBaseUri).log().all();

        if (username != null) {
            request = request.header(SERVICE_AUTHORIZATION_HEADER, serviceToken());
            request = request.header(USER_ID_CONST, username);
        }
        if (userRoles != null) {
            request = request.header(USER_ROLES_CONST, String.join(", ", userRoles));
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
            request = request.header(SERVICE_AUTHORIZATION_HEADER, serviceToken());
            request = request.header(USER_ID_CONST, username);
        }
        if (userRoles != null) {
            request = request.header(USER_ROLES_CONST, String.join(", ", userRoles));
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
            .header(SERVICE_AUTHORIZATION_HEADER, serviceToken())
            .header("cache-control", "no-cache");
    }

    public File file(Object fileName) {
        try {
            return fileUtils.getResourceFile(FILES_FOLDER + fileName);
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

            final InputStream inputStream = givenLargeFileRequest(CITIZEN,
                new ArrayList<>(List.of(CASE_WORKER_ROLE_PROBATE)))
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
        return authTokenProvider.getTokens((String) username, PASSWORD).getUserToken();
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
            .multiPart("files", file(filename != null ? filename : ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", classification != null ? classification : "PUBLIC");

        for (String role : roles) {
            request.multiPart(ROLES_CONST, role);
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
            .multiPart("file", file(filename != null ? filename : ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
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
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, "citizen")
            .multiPart(ROLES_CONST, "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all().statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(ATTACHMENT_9_JPG))
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
        assertArrayEquals(Files.readAllBytes(file(fileName).toPath()), response);
    }

    public void assertLargeDocByteArrayEquality(File file, byte[] response) throws IOException {
        assertArrayEquals(Files.readAllBytes(file.toPath()), response);
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

    public void setToggleTtlEnabled(boolean toggleTtlEnabled) {
        this.toggleTtlEnabled = toggleTtlEnabled;
    }

    public boolean getMetadataMigrationEnabled() {
        return metadataMigrationEnabled;
    }

    public String getCitizen() {
        return CITIZEN;
    }

    public String getCitizen2() {
        return CITIZEN_2;
    }

    public String getCaseWorker() {
        return CASE_WORKER;
    }

    public String getCaseWorkerRoleProbate() {
        return CASE_WORKER_ROLE_PROBATE;
    }

    public String getCustomUserRole() {
        return CUSTOM_USER_ROLE;
    }

    public String getCaseWorkerRoleCmc() {
        return CASE_WORKER_ROLE_CMC;
    }

    public String getCaseWorkerRoleSscs() {
        return CASE_WORKER_ROLE_SSCS;
    }

    public String getCaseWorkerRoleDivorce() {
        return CASE_WORKER_ROLE_DIVORCE;
    }

    public final String getNobody() {
        return NO_BODY;
    }

    public final String getFilesFolder() {
        return FILES_FOLDER;
    }

    public final String getTextAttachment1() {
        return TEXT_ATTACHMENT_1;
    }

    public final String getAttachment2() {
        return ATTACHMENT_2;
    }

    public final String getAttachment3() {
        return ATTACHMENT_3;
    }

    public final String getAttachment4Pdf() {
        return ATTACHMENT_4_PDF;
    }

    public final String getAttachment5() {
        return ATTACHMENT_5;
    }

    public final String getAttachment6Gif() {
        return ATTACHMENT_6_GIF;
    }

    public final String getAttachment7Png() {
        return ATTACHMENT_7_PNG;
    }

    public final String getAttachment8Tif() {
        return ATTACHMENT_8_TIF;
    }

    public final String getAttachment9Jpg() {
        return ATTACHMENT_9_JPG;
    }

    public final String getAttachment10() {
        return ATTACHMENT_10;
    }

    public final String getAttachment11() {
        return ATTACHMENT_11;
    }

    public final String getAttachment15() {
        return ATTACHMENT_15;
    }

    public final String getAttachment16() {
        return ATTACHMENT_16;
    }

    public final String getAttachment17() {
        return ATTACHMENT_17;
    }

    public final String getAttachment18() {
        return ATTACHMENT_18;
    }

    public final String getAttachment19() {
        return ATTACHMENT_19;
    }

    public final String getAttachment20() {
        return ATTACHMENT_20;
    }

    public final String getAttachment21() {
        return ATTACHMENT_21;
    }

    public final String getAttachment22() {
        return ATTACHMENT_22;
    }

    public final String getAttachment23() {
        return ATTACHMENT_23;
    }

    public final String getAttachment24() {
        return ATTACHMENT_24;
    }

    public final String getAttachment25Tiff() {
        return ATTACHMENT_25_TIFF;
    }

    public final String getAttachment26Bmp() {
        return ATTACHMENT_26_BMP;
    }

    public final String getAttachment27Jpeg() {
        return ATTACHMENT_27_JPEG;
    }

    public final String getWord() {
        return WORD;
    }

    public final String getWordMacroEnabledAsRegular() {
        return WORD_MACRO_ENABLED_AS_REGULAR;
    }

    public final String getPowerPoint() {
        return POWER_POINT;
    }

    public final String getExcel() {
        return EXCEL;
    }

    public final String getWordOld() {
        return WORD_OLD;
    }

    public final String getExcelOld() {
        return EXCEL_OLD;
    }

    public final String getPowerPointOld() {
        return POWER_POINT_OLD;
    }

    public final String getWordTemplate() {
        return WORD_TEMPLATE;
    }

    public final String getWordMacroEnabled() {
        return WORD_MACRO_ENABLED;
    }

    public final String getWordTemplateMacroEnabled() {
        return WORD_TEMPLATE_MACRO_ENABLED;
    }

    public final String getExcelTemplate() {
        return EXCEL_TEMPLATE;
    }

    public final String getExcelMacroEnabled() {
        return EXCEL_MACRO_ENABLED;
    }

    public final String getExcelTemplateMacroEnabled() {
        return EXCEL_TEMPLATE_MACRO_ENABLED;
    }

    public final String getPowerPointMacroEnabled() {
        return POWER_POINT_MACRO_ENABLED;
    }

    public final String getPowerPointTemplate() {
        return POWER_POINT_TEMPLATE;
    }

    public final String getPowerPointTemplateMacroEnabled() {
        return POWER_POINT_TEMPLATE_MACRO_ENABLED;
    }

    public final String getPowerPointSlideShow() {
        return POWER_POINT_SLIDE_SHOW;
    }

    public final String getPowerPointSlideShowMacroEnabled() {
        return POWER_POINT_SLIDE_SHOW_MACRO_ENABLED;
    }

    public final String getBadAttachment1() {
        return BAD_ATTACHMENT_1;
    }

    public final String getBadAttachment2() {
        return BAD_ATTACHMENT_2;
    }

    public final String getIllegalNameFile() {
        return ILLEGAL_NAME_FILE;
    }

    public final String getIllegalNameFile1() {
        return ILLEGAL_NAME_FILE_1;
    }

    public final String getIllegalNameFile2() {
        return ILLEGAL_NAME_FILE_2;
    }

    public final String getValidCharFile1() {
        return VALID_CHAR_FILE_1;
    }

    public final String getExeAsPdf() {
        return EXE_AS_PDF;
    }

    public final String getSvgAsPdf() {
        return SVG_AS_PDF;
    }

    public final String getXmlAsPdf() {
        return XML_AS_PDF;
    }

    public final String getExeAsPng() {
        return EXE_AS_PNG;
    }

    public final String getSvgAsPng() {
        return SVG_AS_PNG;
    }

    public final String getXmlAsPng() {
        return XML_AS_PNG;
    }

    public boolean isDropBoxFile() {
        return DROP_BOX_URL.equals(largeDocsBaseUri);
    }
}
