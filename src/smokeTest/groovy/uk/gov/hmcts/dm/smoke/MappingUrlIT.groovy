package uk.gov.hmcts.dm.smoke

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner
import net.thucydides.core.annotations.Pending
import net.thucydides.core.annotations.WithTag
import net.thucydides.core.annotations.WithTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value

import static org.junit.Assert.assertTrue

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags(@WithTag("testType:Smoke"))
class MappingUrlIT extends BaseIT {

    @Value('${toggle.metadatasearchendpoint}')
    boolean metadatasearchendpoint

    @Value('${toggle.folderendpoint}')
    boolean folderendpoint

    @Value('${toggle.documentandmetadatauploadendpoint}')
    boolean documentandmetadatauploadendpoint

    @Value('${toggle.deleteenabled}')
    boolean deleteenabled

    @Value('${toggle.ttl}')
    boolean ttl

    @Value('${toggle.thumbnail}')
    boolean thumbnail

    @Value('${toggle.testing}')
    boolean testing

    List allEndpoints

    @Before
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper()
        JsonNode jsonNode = objectMapper.readTree(givenUnauthenticatedRequest().get("/mappings").print())
        allEndpoints = jsonNode.findValues("predicate").collect { it.asText() }
    }

    @Test
    void "toggle_metadatasearchendpoint toggle Mappings"() {
        assertTrue allEndpoints.any { it ==~ /(.*)(owned)(.*)/ } == metadatasearchendpoint
        assertTrue allEndpoints.any { it ==~ /(.*)(filter)(.*)/ } == metadatasearchendpoint
    }

    @Test
    void "toggle_folderendpoint toggle Mappings"() {
        assertTrue allEndpoints.any { it ==~ /(.*)(folders)(.*)/ } == metadatasearchendpoint
    }


    @Test
    void "toggle_deleteenabled toggle Mappings"() {
        assertTrue allEndpoints.any { it == '{DELETE [/documents/{documentId}]}' } == deleteenabled
    }

    @Test
    void "toggle_thumbnail toggle Mappings"() {
        assertTrue allEndpoints.any { it == '{GET [/documents/{documentId}/thumbnail]}' } == thumbnail
        assertTrue allEndpoints.any { it == '{GET [/documents/{documentId}/versions/{versionId}/thumbnail]}' } ==
            thumbnail
    }

    @Test
    void "toggle_testing toggle Testing"() {
        assertTrue "Testing endpoint should be enabled: ${testing}",
            allEndpoints.any { it == '{GET [/testing/azure-storage-binary-exists/{id}]}' } == testing
    }

}
