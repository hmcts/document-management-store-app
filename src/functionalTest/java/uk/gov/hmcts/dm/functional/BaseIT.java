package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.jcip.annotations.NotThreadSafe;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.dm.functional.DocumentMetadataPropertiesConfig.DocumentMetadata;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;



@NotThreadSafe
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = uk.gov.hmcts.dm.FunctionalTestContextConfiguration.class)
@WithTags(@WithTag("testType:Functional"))
public abstract class BaseIT {

    @Autowired
    private AuthTokenProvider authTokenProvider;
    @Autowired
    private DocumentMetadataPropertiesConfig metadataPropertiesConfig;
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
    private final String PASSWORD = "123";
    private final String CITIZEN = "test12@test.com";
    private String CITIZEN_2 = "test2@test.com";
    private String CASE_WORKER = "test3@test.com";
    private String CASE_WORKER_ROLE_PROBATE = "caseworker-probate";
    private String CUSTOM_USER_ROLE = "custom-user-role";
    private String CASE_WORKER_ROLE_CMC = "caseworker-cmc";
    private String CASE_WORKER_ROLE_SSCS = "caseworker-sscs";
    private String CASE_WORKER_ROLE_DIVORCE = "caseworker-divorce";
    private final String NOBODY = null;
    private final String FILES_FOLDER = "files/";
    private final String TEXT_ATTACHMENT_1 = "Attachment1.txt";
    private final String ATTACHMENT_2 = "Attachment2.txt";
    private final String ATTACHMENT_3 = "Attachment3.txt";
    private final String ATTACHMENT_4_PDF = "1MB.PDF";
    private final String ATTACHMENT_5 = "Attachment1.csv";
    private final String ATTACHMENT_6_GIF = "marbles.gif";
    private final String ATTACHMENT_7_PNG = "png.png";
    private final String ATTACHMENT_8_TIF = "tif.tif";
    private final String ATTACHMENT_9_JPG = "jpg.jpg";
    private final String ATTACHMENT_10 = "svg.svg";
    private final String ATTACHMENT_11 = "rtf.rtf";
    private final String ATTACHMENT_15 = "odt.odt";
    private final String ATTACHMENT_16 = "ods.ods";
    private final String ATTACHMENT_17 = "odp.odp";
    private final String ATTACHMENT_18 = "xml.xml";
    private final String ATTACHMENT_19 = "wav.wav";
    private final String ATTACHMENT_20 = "mid.mid";
    private final String ATTACHMENT_21 = "mp3.mp3";
    private final String ATTACHMENT_22 = "webm.webm";
    private final String ATTACHMENT_23 = "ogg.ogg";
    private final String ATTACHMENT_24 = "mp4.mp4";
    private final String ATTACHMENT_25_TIFF = "tiff.tiff";
    private final String ATTACHMENT_26_BMP = "bmp.bmp";
    private final String ATTACHMENT_27_JPEG = "jpeg.jpeg";
    private final String WORD = "docx.docx";
    private final String WORD_MACRO_ENABLED_AS_REGULAR = "docmHidden.docx";
    private final String POWER_POINT = "pptx.pptx";
    private final String EXCEL = "xlsx.xlsx";
    private final String WORD_OLD = "doc.doc";
    private final String EXCEL_OLD = "xls.xls";
    private final String POWER_POINT_OLD = "ppt.ppt";
    private final String WORD_TEMPLATE = "dotx.dotx";
    private final String WORD_MACRO_ENABLED = "docm.docm";
    private final String WORD_TEMPLATE_MACRO_ENABLED = "dotm.dotm";
    private final String EXCEL_TEMPLATE = "xltx.xltx";
    private final String EXCEL_MACRO_ENABLED = "xlsm.xlsm";
    private final String EXCEL_TEMPLATE_MACRO_ENABLED = "xltm.xltm";
    private final String POWER_POINT_MACRO_ENABLED = "pptm.pptm";
    private final String POWER_POINT_TEMPLATE = "potx.potx";
    private final String POWER_POINT_TEMPLATE_MACRO_ENABLED = "potm.potm";
    private final String POWER_POINT_SLIDE_SHOW = "ppsx.ppsx";
    private final String POWER_POINT_SLIDE_SHOW_MACRO_ENABLED = "ppsm.ppsm";
    private final String THUMBNAIL_PDF = "thumbnailPDF.jpg";
    private final String THUMBNAIL_BMP = "thumbnailBMP.jpg";
    private final String THUMBNAIL_GIF = "thumbnailGIF.jpg";
    private final String BAD_ATTACHMENT_1 = "1MB.exe";
    private final String BAD_ATTACHMENT_2 = "Attachment3.zip";
    private final String ILLEGAL_NAME_FILE = "uploadFile~@$!.jpg";
    private final String ILLEGAL_NAME_FILE1 = "uploadFile~`';][{}!@£$%^&()}{_-.jpg";
    private final String ILLEGAL_NAME_FILE2 = "uploadFile9 @_-.jpg";
    private final String VALID_CHAR_FILE1 = "uploadFile 9.txt";
    private final String EXE_AS_PDF = "exe_as_pdf.pdf";
    private final String SVG_AS_PDF = "svg_as_pdf.pdf";
    private final String XML_AS_PDF = "xml_as_pdf.pdf";
    private final String EXE_AS_PNG = "exe_as_png.png";
    private final String SVG_AS_PNG = "svg_as_png.png";
    private final String XML_AS_PNG = "xml_as_png.png";

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

        try(
            OutputStream outputStream = new FileOutputStream(tmpFile);
            final InputStream inputStream = givenLargeFileRequest(CITIZEN, new ArrayList<>(List.of(CASE_WORKER_ROLE_PROBATE)))
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

    public Response fetchDocumentMetaDataAs(String username, String documentUrl) {
        return givenRequest(username).get(documentUrl);
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

    public Response createAUserForTTL(String username) {
        return givenRequest(username)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
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

    public String getCITIZEN() {
        return CITIZEN;
    }

    public String getCITIZEN_2() {
        return CITIZEN_2;
    }

    public void setCITIZEN_2(String CITIZEN_2) {
        this.CITIZEN_2 = CITIZEN_2;
    }

    public String getCASE_WORKER() {
        return CASE_WORKER;
    }

    public void setCASE_WORKER(String CASE_WORKER) {
        this.CASE_WORKER = CASE_WORKER;
    }

    public String getCASE_WORKER_ROLE_PROBATE() {
        return CASE_WORKER_ROLE_PROBATE;
    }

    public void setCASE_WORKER_ROLE_PROBATE(String CASE_WORKER_ROLE_PROBATE) {
        this.CASE_WORKER_ROLE_PROBATE = CASE_WORKER_ROLE_PROBATE;
    }

    public String getCUSTOM_USER_ROLE() {
        return CUSTOM_USER_ROLE;
    }

    public void setCUSTOM_USER_ROLE(String CUSTOM_USER_ROLE) {
        this.CUSTOM_USER_ROLE = CUSTOM_USER_ROLE;
    }

    public String getCASE_WORKER_ROLE_CMC() {
        return CASE_WORKER_ROLE_CMC;
    }

    public void setCASE_WORKER_ROLE_CMC(String CASE_WORKER_ROLE_CMC) {
        this.CASE_WORKER_ROLE_CMC = CASE_WORKER_ROLE_CMC;
    }

    public String getCASE_WORKER_ROLE_SSCS() {
        return CASE_WORKER_ROLE_SSCS;
    }

    public void setCASE_WORKER_ROLE_SSCS(String CASE_WORKER_ROLE_SSCS) {
        this.CASE_WORKER_ROLE_SSCS = CASE_WORKER_ROLE_SSCS;
    }

    public String getCASE_WORKER_ROLE_DIVORCE() {
        return CASE_WORKER_ROLE_DIVORCE;
    }

    public void setCASE_WORKER_ROLE_DIVORCE(String CASE_WORKER_ROLE_DIVORCE) {
        this.CASE_WORKER_ROLE_DIVORCE = CASE_WORKER_ROLE_DIVORCE;
    }

    public final String getNOBODY() {
        return NOBODY;
    }

    public final String getFILES_FOLDER() {
        return FILES_FOLDER;
    }

    public final String getTEXT_ATTACHMENT_1() {
        return TEXT_ATTACHMENT_1;
    }

    public final String getATTACHMENT_2() {
        return ATTACHMENT_2;
    }

    public final String getATTACHMENT_3() {
        return ATTACHMENT_3;
    }

    public final String getATTACHMENT_4_PDF() {
        return ATTACHMENT_4_PDF;
    }

    public final String getATTACHMENT_5() {
        return ATTACHMENT_5;
    }

    public final String getATTACHMENT_6_GIF() {
        return ATTACHMENT_6_GIF;
    }

    public final String getATTACHMENT_7_PNG() {
        return ATTACHMENT_7_PNG;
    }

    public final String getATTACHMENT_8_TIF() {
        return ATTACHMENT_8_TIF;
    }

    public final String getATTACHMENT_9_JPG() {
        return ATTACHMENT_9_JPG;
    }

    public final String getATTACHMENT_10() {
        return ATTACHMENT_10;
    }

    public final String getATTACHMENT_11() {
        return ATTACHMENT_11;
    }

    public final String getATTACHMENT_15() {
        return ATTACHMENT_15;
    }

    public final String getATTACHMENT_16() {
        return ATTACHMENT_16;
    }

    public final String getATTACHMENT_17() {
        return ATTACHMENT_17;
    }

    public final String getATTACHMENT_18() {
        return ATTACHMENT_18;
    }

    public final String getATTACHMENT_19() {
        return ATTACHMENT_19;
    }

    public final String getATTACHMENT_20() {
        return ATTACHMENT_20;
    }

    public final String getATTACHMENT_21() {
        return ATTACHMENT_21;
    }

    public final String getATTACHMENT_22() {
        return ATTACHMENT_22;
    }

    public final String getATTACHMENT_23() {
        return ATTACHMENT_23;
    }

    public final String getATTACHMENT_24() {
        return ATTACHMENT_24;
    }

    public final String getATTACHMENT_25_TIFF() {
        return ATTACHMENT_25_TIFF;
    }

    public final String getATTACHMENT_26_BMP() {
        return ATTACHMENT_26_BMP;
    }

    public final String getATTACHMENT_27_JPEG() {
        return ATTACHMENT_27_JPEG;
    }

    public final String getWORD() {
        return WORD;
    }

    public final String getWORD_MACRO_ENABLED_AS_REGULAR() {
        return WORD_MACRO_ENABLED_AS_REGULAR;
    }

    public final String getPOWER_POINT() {
        return POWER_POINT;
    }

    public final String getEXCEL() {
        return EXCEL;
    }

    public final String getWORD_OLD() {
        return WORD_OLD;
    }

    public final String getEXCEL_OLD() {
        return EXCEL_OLD;
    }

    public final String getPOWER_POINT_OLD() {
        return POWER_POINT_OLD;
    }

    public final String getWORD_TEMPLATE() {
        return WORD_TEMPLATE;
    }

    public final String getWORD_MACRO_ENABLED() {
        return WORD_MACRO_ENABLED;
    }

    public final String getWORD_TEMPLATE_MACRO_ENABLED() {
        return WORD_TEMPLATE_MACRO_ENABLED;
    }

    public final String getEXCEL_TEMPLATE() {
        return EXCEL_TEMPLATE;
    }

    public final String getEXCEL_MACRO_ENABLED() {
        return EXCEL_MACRO_ENABLED;
    }

    public final String getEXCEL_TEMPLATE_MACRO_ENABLED() {
        return EXCEL_TEMPLATE_MACRO_ENABLED;
    }

    public final String getPOWER_POINT_MACRO_ENABLED() {
        return POWER_POINT_MACRO_ENABLED;
    }

    public final String getPOWER_POINT_TEMPLATE() {
        return POWER_POINT_TEMPLATE;
    }

    public final String getPOWER_POINT_TEMPLATE_MACRO_ENABLED() {
        return POWER_POINT_TEMPLATE_MACRO_ENABLED;
    }

    public final String getPOWER_POINT_SLIDE_SHOW() {
        return POWER_POINT_SLIDE_SHOW;
    }

    public final String getPOWER_POINT_SLIDE_SHOW_MACRO_ENABLED() {
        return POWER_POINT_SLIDE_SHOW_MACRO_ENABLED;
    }

    public final String getTHUMBNAIL_PDF() {
        return THUMBNAIL_PDF;
    }

    public final String getTHUMBNAIL_BMP() {
        return THUMBNAIL_BMP;
    }

    public final String getTHUMBNAIL_GIF() {
        return THUMBNAIL_GIF;
    }

    public final String getBAD_ATTACHMENT_1() {
        return BAD_ATTACHMENT_1;
    }

    public final String getBAD_ATTACHMENT_2() {
        return BAD_ATTACHMENT_2;
    }

    public final String getILLEGAL_NAME_FILE() {
        return ILLEGAL_NAME_FILE;
    }

    public final String getILLEGAL_NAME_FILE1() {
        return ILLEGAL_NAME_FILE1;
    }

    public final String getILLEGAL_NAME_FILE2() {
        return ILLEGAL_NAME_FILE2;
    }

    public final String getVALID_CHAR_FILE1() {
        return VALID_CHAR_FILE1;
    }

    public final String getEXE_AS_PDF() {
        return EXE_AS_PDF;
    }

    public final String getSVG_AS_PDF() {
        return SVG_AS_PDF;
    }

    public final String getXML_AS_PDF() {
        return XML_AS_PDF;
    }

    public final String getEXE_AS_PNG() {
        return EXE_AS_PNG;
    }

    public final String getSVG_AS_PNG() {
        return SVG_AS_PNG;
    }

    public final String getXML_AS_PNG() {
        return XML_AS_PNG;
    }
}
