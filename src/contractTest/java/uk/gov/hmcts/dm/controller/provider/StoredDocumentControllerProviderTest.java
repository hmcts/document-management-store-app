package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;

@Provider("dm_store_stored_document_provider")
public class StoredDocumentControllerProviderTest extends BaseProviderTest {

    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private StoredDocument createSampleStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC);
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setModifiedOn(new Date());
        storedDocument.setRoles(Set.of("citizen"));
        return storedDocument;
    }

    @State("A Stored Document exists and can be retrieved by documentId")
    public void storedDocumentExists() {
        StoredDocument storedDocument = createSampleStoredDocument();
        when(auditedStoredDocumentOperationsService.readStoredDocument(UUID.fromString(DOCUMENT_ID)))
            .thenReturn(storedDocument);
    }

}
