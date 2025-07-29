package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Provider("dm_store_update_document_provider")
public class StoredDocumentUpdateControllerProviderTest extends BaseProviderTest {


    @State("Documents exist and can be updated with new TTL")
    public void documentsExistToUpdate() {
        when(auditedStoredDocumentOperationsService.updateDocument(
            any(UUID.class),
            any(Map.class),
            any(Date.class))
        ).thenReturn(new StoredDocument());
    }


    @State("Document exist and can be updated with new TTL")
    public void specificDocumentExistToUpdate() {

        UUID documentId = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

        StoredDocument storedDocument = StoredDocument.builder()
            .id(documentId)
            .createdBy("user@example.com")
            .createdByService("some-service")
            .lastModifiedBy("user@example.com")
            .lastModifiedByService("some-service")
            .createdOn(new Date())
            .modifiedOn(new Date())
            .deleted(false)
            .hardDeleted(false)
            .classification(Classifications.PRIVATE)
            .roles(Set.of("citizen", "caseworker"))
            .metadata(Map.of("caseId", "123456", "docType", "evidence"))
            .ttl(new Date(System.currentTimeMillis() + 86400000L)) // 1 day in future
            .documentContentVersions(List.of(mockDocumentContentVersion(documentId)))
            .auditEntries(Set.of())
            .build();

        when(auditedStoredDocumentOperationsService.updateDocument(
            any(UUID.class),
            any(Map.class),
            any(Date.class))
        ).thenReturn(storedDocument);
    }

    private DocumentContentVersion mockDocumentContentVersion(UUID documentId) {
        DocumentContentVersion version = new DocumentContentVersion();
        version.setId(UUID.randomUUID());
        version.setStoredDocument(new StoredDocument(documentId));
        version.setCreatedOn(new Date());
        version.setMimeType("application/pdf");
        version.setSize(2048L);
        version.setCreatedBy("user@example.com");
        version.setCreatedByService("some-service");
        return version;
    }
}
