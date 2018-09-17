package uk.gov.hmcts.dm.smoke

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.junit4.SpringRunner

import static org.junit.Assert.assertNotNull
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

    def request

    @Test
    void "Normal Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertNotNull(request["{[/documents],methods=[POST],consumes=[multipart/form-data]}"])

        assertNotNull(request["{[/documents/{documentId}],methods=[GET]}"])
        assertNotNull(request["{[/documents/{documentId}/binary],methods=[GET]}"])

        assertNotNull(request["{[/documents/{documentId}],methods=[POST],consumes=[multipart/form-data]}"])
        assertNotNull(request["{[/documents/{documentId}/versions],methods=[POST],consumes=[multipart/form-data]}"])

        assertNotNull(request["{[/documents/{documentId}/versions/{versionId}],methods=[GET]}"])
        assertNotNull(request["{[/documents/{documentId}/versions/{versionId}/binary],methods=[GET]}"])

        assertNotNull(request["{[/documents/{documentId}/auditEntries],methods=[GET]}"])
    }

    @Test
    void "toggle.metadatasearchendpoint toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertTrue(metadatasearchendpoint == (null != request["{[/documents/owned],methods=[POST]}"]))
        assertTrue(metadatasearchendpoint == (null != request["{[/documents/filter],methods=[POST],consumes=[application/json]}"]))
    }


    @Test
    @Ignore("Not Testable")
    void "toggle.documentandmetadatauploadendpoint toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')
//        assertTrue(documentandmetadatauploadendpoint == (null != request[""]))
    }

    @Test
    void "toggle.folderendpoint toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertTrue(folderendpoint == (null != request["{[/folders/{id}/documents],methods=[POST],consumes=[multipart/form-data],produces=[application/vnd.uk.gov.hmcts.dm.folder.v1+json;charset=UTF-8]}"]))
        assertTrue(folderendpoint == (null != request["{[/folders/{id}],methods=[GET],produces=[application/vnd.uk.gov.hmcts.dm.folder.v1+json;charset=UTF-8]}"]))
        assertTrue(folderendpoint == (null != request["{[/folders/{id}],methods=[DELETE],produces=[application/vnd.uk.gov.hmcts.dm.folder.v1+json;charset=UTF-8]}"]))
        assertTrue(folderendpoint == (null != request["{[/folders],methods=[POST],produces=[application/vnd.uk.gov.hmcts.dm.folder.v1+json;charset=UTF-8]}"]))
    }


    @Test
    void "toggle.deleteenabled toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertTrue(deleteenabled == (null != request["{[/documents/{documentId}],methods=[DELETE]}"]))
    }

    @Test
    void "toggle.ttl toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertTrue(ttl == (null != request["{[/documents/{documentId}],methods=[PATCH],consumes=[application/json]}"]))
    }

    @Test
    void "toggle.thumbnail toggle Mappings"() {
        request = givenUnauthenticatedRequest().get("/mappings").path('')

        assertTrue(thumbnail == (null != request["{[/documents/{documentId}/thumbnail],methods=[GET]}"]))
        assertTrue(thumbnail == (null != request["{[/documents/{documentId}/versions/{versionId}/thumbnail],methods=[GET]}"]))
    }

}
