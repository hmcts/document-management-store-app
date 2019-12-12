package uk.gov.hmcts.dm.smoke

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.junit4.SpringRunner

import static org.junit.Assert.assertTrue

@RunWith(SpringRunner.class)
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
    @Ignore("Not Testable")
    void "toggle_documentandmetadatauploadendpoint toggle Mappings"() {
        assertTrue allEndpoints.any { it ==~ /(.*)(mappings)(.*)/ } == metadatasearchendpoint
    }

    @Test
    void "toggle_folderendpoint toggle Mappings"() {
        assertTrue allEndpoints.any { it ==~ /(.*)(folders)(.*)/ } == metadatasearchendpoint
    }


    @Test
    void "toggle_deleteenabled toggle Mappings"() {
        assertTrue allEndpoints.any { it == '{DELETE /documents/{documentId}}' } == deleteenabled
    }

    @Test
    void "toggle_ttl toggle Mappings"() {
        assertTrue allEndpoints.any { it == '{PATCH /documents/{documentId}, consumes [application/json]}' } == ttl
    }

    @Test
    void "toggle_thumbnail toggle Mappings"() {
        assertTrue allEndpoints.any { it == '{GET /documents/{documentId}/thumbnail}' } == thumbnail
        assertTrue allEndpoints.any { it == '{GET /documents/{documentId}/versions/{versionId}/thumbnail}' } == thumbnail
    }

    @Test
    void "toggle_testing toggle Testing"() {
        assertTrue "Testing endpoint should be enabled: ${testing}",  allEndpoints.any { it == '{GET /testing/azure-storage-binary-exists/{id}}' } == testing
    }

}
