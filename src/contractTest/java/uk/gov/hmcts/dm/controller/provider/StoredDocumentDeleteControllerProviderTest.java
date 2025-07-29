package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Provider("dm_store_delete_document_provider")
public class StoredDocumentDeleteControllerProviderTest extends BaseProviderTest {

    @State("Document exists and can be deleted")
    public void documentExistToDelete() {
        doNothing().when(auditedStoredDocumentOperationsService).deleteStoredDocument(any(UUID.class), anyBoolean());
    }

    @State("Document exists and can be soft deleted")
    public void documentExistToSoftDelete() {
        when(searchService.findStoredDocumentsIdsByCaseRef(any()))
            .thenReturn(List.of(new StoredDocument()));
        when(auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(any()))
            .thenReturn(new CaseDocumentsDeletionResults(5, 4));
    }
}
