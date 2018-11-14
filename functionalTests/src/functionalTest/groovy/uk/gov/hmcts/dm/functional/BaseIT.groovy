package uk.gov.hmcts.dm.functional

import io.restassured.RestAssured
import io.restassured.response.Response
import net.jcip.annotations.NotThreadSafe
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.FileUtils
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes

import javax.annotation.PostConstruct
import uk.gov.hmcts.dm.functional.config.FunctionalTestContextConfiguration

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.expect
import static org.hamcrest.Matchers.equalTo

@ContextConfiguration(classes = FunctionalTestContextConfiguration)
@NotThreadSafe
class BaseIT {

    @Autowired
    ToggleConfiguration toggleConfiguration;

    @Autowired
    AuthTokenProvider authTokenProvider

    FileUtils fileUtils = new FileUtils()

    @Value('${base-urls.dm-store}')
    String dmStoreBaseUri

    final String PASSWORD = '123'

    String CITIZEN = 'test12@test.com'

    String CITIZEN_2 = 'test2@test.com'

    String CASE_WORKER = 'test3@test.com'

    String CASE_WORKER_ROLE_PROBATE = 'caseworker-probate'
    String CASE_WORKER_ROLE_CMC = 'caseworker-cmc'
    String CASE_WORKER_ROLE_SSCS = 'caseworker-sscs'
    String CASE_WORKER_ROLE_DIVORCE = 'caseworker-divorce'

    final String NOBODY = null

    final String FILES_FOLDER = 'files/'
    final String TEXT_ATTACHMENT_1 = 'Attachment1.txt'
    final String ATTACHMENT_2 = 'Attachment2.txt'
    final String ATTACHMENT_3 = 'Attachment3.txt'
    final String ATTACHMENT_4_PDF = '1MB.PDF'
    final String ATTACHMENT_5 = 'Attachment1.csv'
    final String ATTACHMENT_6_GIF = 'marbles.gif'
    final String ATTACHMENT_7_PNG = 'png.png'
    final String ATTACHMENT_8_TIF = 'tif.tif'
    final String ATTACHMENT_9_JPG = 'jpg.jpg'
    final String ATTACHMENT_10 = 'svg.svg'
    final String ATTACHMENT_11 = 'rtf.rtf'

    final String ATTACHMENT_15 = 'odt.odt'
    final String ATTACHMENT_16 = 'ods.ods'
    final String ATTACHMENT_17 = 'odp.odp'
    final String ATTACHMENT_18 = 'xml.xml'
    final String ATTACHMENT_19 = 'wav.wav'
    final String ATTACHMENT_20 = 'mid.mid'
    final String ATTACHMENT_21 = 'mp3.mp3'
    final String ATTACHMENT_22 = 'webm.webm'
    final String ATTACHMENT_23 = 'ogg.ogg'
    final String ATTACHMENT_24 = 'mp4.mp4'
    final String ATTACHMENT_25_TIFF = 'tiff.tiff'
    final String ATTACHMENT_26_BMP = 'bmp.bmp'
    final String ATTACHMENT_27_JPEG = 'jpeg.jpeg'

    final String WORD = 'docx.docx'
    final String WORD_MACRO_ENABLED_AS_REGULAR = 'docmHidden.docx'
    final String POWER_POINT = 'pptx.pptx'
    final String EXCEL = 'xlsx.xlsx'



    final String WORD_OLD = 'doc.doc'
    final String EXCEL_OLD = 'xls.xls'
    final String POWER_POINT_OLD = 'ppt.ppt'

    final String WORD_TEMPLATE = 'dotx.dotx'
    final String WORD_MACRO_ENABLED = 'docm.docm'
    final String WORD_TEMPLATE_MACRO_ENABLED = 'dotm.dotm'

    final String EXCEL_TEMPLATE = 'xltx.xltx'
    final String EXCEL_MACRO_ENABLED = 'xlsm.xlsm'
    final String EXCEL_TEMPLATE_MACRO_ENABLED = 'xltm.xltm'

    final String POWER_POINT_MACRO_ENABLED = 'pptm.pptm'
    final String POWER_POINT_TEMPLATE = 'potx.potx'
    final String POWER_POINT_TEMPLATE_MACRO_ENABLED = 'potm.potm'
    final String POWER_POINT_SLIDE_SHOW = 'ppsx.ppsx'
    final String POWER_POINT_SLIDE_SHOW_MACRO_ENABLED = 'ppsm.ppsm'

    final String THUMBNAIL_PDF = 'thumbnailPDF.jpg'
    final String THUMBNAIL_BMP = 'thumbnailBMP.jpg'
    final String THUMBNAIL_GIF = 'thumbnailGIF.jpg'


    final String BAD_ATTACHMENT_1 = '1MB.exe'
    final String BAD_ATTACHMENT_2 = 'Attachment3.zip'
    final String MAX_SIZE_ALLOWED_ATTACHMENT = '90MB.pdf'
    final String TOO_LARGE_ATTACHMENT = '100MB.pdf'
    final String ILLEGAL_NAME_FILE = 'uploadFile~@$!.jpg'
    final String ILLEGAL_NAME_FILE1 = 'uploadFile~`\';][{}!@£$%^&()}{_-.jpg'
    final String ILLEGAL_NAME_FILE2 = 'uploadFile9 @_-.jpg'
    final String VALID_CHAR_FILE1= 'uploadFile 9.txt'

    final String EXE_AS_PDF= 'exe_as_pdf.pdf'
    final String SVG_AS_PDF = 'svg_as_pdf.pdf'
    final String XML_AS_PDF = 'xml_as_pdf.pdf'

    final String EXE_AS_PNG= 'exe_as_png.png'
    final String SVG_AS_PNG = 'svg_as_png.png'
    final String XML_AS_PNG = 'xml_as_png.png'

    @PostConstruct
    void init() {
        RestAssured.baseURI = dmStoreBaseUri
        RestAssured.useRelaxedHTTPSValidation()
    }

    def givenUnauthenticatedRequest() {
        def request = given().log().all()
        request
    }

    def givenRequest(username = null, userRoles = null) {

        def request = given().log().all()

        if (username) {
            request = request.header("serviceauthorization", serviceToken())
            if (username) {
                request = request.header("user-id", username)
            }
            if (userRoles) {
                request = request.header("user-roles", userRoles.join(','))
            }
        }

        request
    }

    def givenS2SRequest() {
        given().log().all()
            .header("serviceauthorization", serviceToken())
            .header("cache-control", "no-cache")
    }

    def expectRequest() {
        expect().log().all()
    }

    def file(fileName) {
        try {
            fileUtils.getResourceFile(FILES_FOLDER + fileName)
        } catch (e) {
            e.printStackTrace()
            throw e
        }
    }

    def authToken(username) {
        def token = authTokenProvider.getTokens(username, PASSWORD).getUserToken()
        token
    }

    def userId(token) {
        authTokenProvider.findUserId(token).toString()
    }

    def serviceToken() {
        authTokenProvider.findServiceToken()
    }

    def createDocument(username,  filename = null, classification = null, roles = null, metadata = null) {
        def request = givenRequest(username)
                        .multiPart("files", file( filename ?: ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
                        .multiPart("classification", classification ?: "PUBLIC")

        roles?.each { role ->
            request.multiPart("roles", role)
        }

        if (metadata) {
            request.accept(V1MediaTypes.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE_VALUE)
            metadata?.each { key, value ->
                request.multiPart("metadata[${key}]", value)
            }
        }

        request
            .expect()
                .statusCode(200)
            .when()
                .post("/documents")
    }

    def createDocumentAndGetUrlAs(username, filename = null, classification = null, roles = null, metadata = null) {
        createDocument(username, filename, classification, roles, metadata)
            .path("_embedded.documents[0]._links.self.href")
    }

    def createDocumentAndGetBinaryUrlAs(username,  filename = null, classification = null, roles = null) {
        createDocument(username, filename, classification, roles)
            .path("_embedded.documents[0]._links.binary.href")
    }

    def createDocumentContentVersion(documentUrl, username, filename = null) {
        givenRequest(username)
            .multiPart("file", file( filename ?: ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .expect()
                .statusCode(201)
            .when()
                .post(documentUrl)
    }

    def createDocumentContentVersionAndGetUrlAs(documentUrl, username, filename = null) {
        createDocumentContentVersion(documentUrl, username, filename).path('_links.self.href')
    }

    def createDocumentContentVersionAndGetBinaryUrlAs(documentUrl, username, filename = null) {
        createDocumentContentVersion(documentUrl, username, filename).path('_links.binary.href')
    }

    def CreateAUserforTTL(username) {
        Response response = givenRequest(username)
            .multiPart("files", file(ATTACHMENT_9_JPG), MediaType.IMAGE_JPEG_VALUE)
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .multiPart("ttl", "2018-10-31T10:10:10+0000")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(ATTACHMENT_9_JPG))
            .body("_embedded.documents[0].mimeType", equalTo(MediaType.IMAGE_JPEG_VALUE))
            .body("_embedded.documents[0].classification", equalTo(Classifications.PUBLIC as String))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].ttl", equalTo("2018-10-31T10:10:10+0000"))
            .when()
            .post("/documents")

        response
    }

    void assertByteArrayEquality(String fileName, byte[] response) {
        Assert.assertTrue(Arrays.equals(file(fileName).bytes, response))
    }

}
