package uk.gov.hmcts.dm.smoke


import net.jcip.annotations.NotThreadSafe
import net.serenitybdd.rest.SerenityRest
import net.thucydides.core.annotations.WithTag
import net.thucydides.core.annotations.WithTags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration
import uk.gov.hmcts.dm.smoke.utilities.FileUtils
import uk.gov.hmcts.dm.smoke.utilities.V1MediaTypes

import javax.annotation.PostConstruct

@ContextConfiguration(classes = SmokeTestContextConfiguration)
@NotThreadSafe
@WithTags(@WithTag("testType:Smoke"))
class BaseIT {

    @Autowired
    AuthTokenProvider authTokenProvider

    FileUtils fileUtils = new FileUtils()

    @Value('${base-urls.dm-store}')
    String dmStoreBaseUri

    final String FILES_FOLDER = 'files/'
    final String ATTACHMENT_1 = 'Attachment1.txt'

    @PostConstruct
    void init() {
        SerenityRest.useRelaxedHTTPSValidation()
    }

    def givenUnauthenticatedRequest() {
        SerenityRest
            .given()
            .baseUri(dmStoreBaseUri)
            .log().all()
    }

    def givenRequest() {
        SerenityRest
            .given()
            .baseUri(dmStoreBaseUri)
            .log().all().header("ServiceAuthorization", serviceToken())
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

}
