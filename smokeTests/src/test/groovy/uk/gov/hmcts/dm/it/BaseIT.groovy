package uk.gov.hmcts.dm.it

import io.restassured.RestAssured
import net.jcip.annotations.NotThreadSafe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import uk.gov.hmcts.dm.it.utilities.FileUtils
import uk.gov.hmcts.dm.it.utilities.V1MediaTypes

import javax.annotation.PostConstruct
import uk.gov.hmcts.dm.it.config.TestContextConfiguration

import static io.restassured.RestAssured.given

/**
 * Created by pawel on 16/10/2017.
 */
@ContextConfiguration(classes = TestContextConfiguration)
@NotThreadSafe
class BaseIT {

    @Autowired
    AuthTokenProvider authTokenProvider

    FileUtils fileUtils = new FileUtils()

    @Value('${base-urls.dm-api}')
    String dmGwBaseUri

    final String FILES_FOLDER = 'files/'
    final String ATTACHMENT_1 = 'Attachment1.txt'
    final String ATTACHMENT_2 = 'Attachment2.txt'
    final String ATTACHMENT_3 = 'Attachment3.txt'
    final String ATTACHMENT_4 = '1MB.PDF'
    final String ATTACHMENT_5 = 'Attachment1.csv'
    final String ATTACHMENT_6 = 'marbles.gif'
    final String ATTACHMENT_7 = 'png.png'
    final String ATTACHMENT_8 = 'tif.tif'
    final String ATTACHMENT_9 = 'jpg.jpg'
    final String ATTACHMENT_10 = 'svg.svg'
    final String ATTACHMENT_11 = 'rtf.rtf'
    final String ATTACHMENT_12 = 'docx.docx'
    final String ATTACHMENT_13 = 'pptx.pptx'
    final String ATTACHMENT_14 = 'xlsx.xlsx'
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
    final String ATTACHMENT_25 = 'tiff.tiff'
    final String ATTACHMENT_26 = 'bmp.bmp'

    final String BAD_ATTACHMENT_1 = '1MB.exe'
    final String BAD_ATTACHMENT_2 = 'Attachment3.zip'
    final String MAX_SIZE_ALLOWED_ATTACHMENT = '90MB.pdf'
    final String TOO_LARGE_ATTACHMENT = '100MB.pdf'
    final String ILLEGAL_CHAR_FILE = 'uploadFile~@$!.txt'
    final String ILLEGAL_CHAR_FILE1 = 'uploadFile~`\';][{}!@£$%^&()}{_-.txt'
    final String ILLEGAL_CHAR_FILE2 = 'uploadFile9 @_-.txt'
    final String VALID_CHAR_FILE1 = 'uploadFile 9.txt'

    @PostConstruct
    void init() {
        RestAssured.baseURI = dmGwBaseUri
    }

    def givenRequest() {
        def request = given().log().all()
        request = request.header("ServiceAuthorization", serviceToken())
        request
    }

    def serviceToken() {
        authTokenProvider.findServiceToken()
    }

    def file(fileName) {
        fileUtils.getResourceFile(FILES_FOLDER + fileName)
    }

    def createDocument(filename = null, classification = null, roles = null, metadata = null) {
        def request = givenRequest()
            .multiPart("files", file(filename ?: "Attachment1.txt"), MediaType.TEXT_PLAIN_VALUE)
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

    def createDocumentAndGetUrlAs(filename = null, classification = null, roles = null, metadata = null) {
        createDocument(filename, classification, roles, metadata)
            .path("_embedded.documents[0]._links.self.href")
    }

    def createDocumentAndGetBinaryUrlAs(filename = null, classification = null, roles = null) {
        createDocument(filename, classification, roles)
            .path("_embedded.documents[0]._links.binary.href")
    }

    def createDocumentContentVersion(documentUrl, filename = null) {
        givenRequest()
            .multiPart("file", file(filename ?: ATTACHMENT_1), MediaType.TEXT_PLAIN_VALUE)
            .expect()
            .statusCode(201)
            .when()
            .post(documentUrl)
    }

    def createDocumentContentVersionAndGetUrlAs(documentUrl, filename = null) {
        createDocumentContentVersion(documentUrl, filename).path('_links.self.href')
    }

    def createDocumentContentVersionAndGetBinaryUrlAs(documentUrl, filename = null) {
        createDocumentContentVersion(documentUrl, filename).path('_links.binary.href')
    }

    def givenUnauthenticatedRequest() {
        def request = given().log().all()
        request
    }
}
