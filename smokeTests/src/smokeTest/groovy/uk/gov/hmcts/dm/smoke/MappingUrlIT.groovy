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

    List allEndpoints

    @Before
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper()
        JsonNode jsonNode = objectMapper.readTree(givenUnauthenticatedRequest().get("/mappings").print())
        allEndpoints = jsonNode.findValues("predicate").collect { it.asText() }
    }

    @Test
    void "toggle.metadatasearchendpoint toggle Mappings"() {
        assertTrue allEndpoints.find { it ==~ /(.*)(owned)(.*)/ } as Boolean == metadatasearchendpoint
        assertTrue allEndpoints.find { it ==~ /(.*)(filter)(.*)/ } as Boolean == metadatasearchendpoint
    }

    @Test
    @Ignore("Not Testable")
    void "toggle.documentandmetadatauploadendpoint toggle Mappings"() {
        assertTrue allEndpoints.find { it ==~ /(.*)(mappings)(.*)/ } as Boolean == metadatasearchendpoint
    }

    @Test
    void "toggle.folderendpoint toggle Mappings"() {
        assertTrue allEndpoints.find { it ==~ /(.*)(folders)(.*)/ } as Boolean == metadatasearchendpoint
    }


    @Test
    void "toggle.deleteenabled toggle Mappings"() {
        assertTrue allEndpoints.find { it == '{DELETE /documents/{documentId}}' } as Boolean == deleteenabled
    }

    @Test
    void "toggle.ttl toggle Mappings"() {
        assertTrue allEndpoints.find { it == '{PATCH /documents/{documentId}, consumes [application/json]}' } as Boolean == ttl
    }

    @Test
    void "toggle.thumbnail toggle Mappings"() {
        assertTrue allEndpoints.find { it == '{GET /documents/{documentId}/thumbnail}' } as Boolean == thumbnail
        assertTrue allEndpoints.find { it == '{GET /documents/{documentId}/versions/{versionId}/thumbnail}' } as Boolean == thumbnail
    }

}
