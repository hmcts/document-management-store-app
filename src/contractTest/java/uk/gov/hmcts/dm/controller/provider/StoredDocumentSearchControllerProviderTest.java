package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Provider("dm_store_stored_document_search_provider")
public class StoredDocumentSearchControllerProviderTest extends BaseProviderTest {
    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private StoredDocument createSampleStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC);
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setRoles(Set.of("citizen"));
        return storedDocument;
    }

    @State("Documents exist matching metadata search criteria-filter by metadata")
    public void documentsExistForMetadataSearch() {
        StoredDocument doc = createSampleStoredDocument();
        Page<StoredDocument> page = new PageImpl<>(List.of(doc));
        when(searchService.findStoredDocumentsByMetadata(any(), any())).thenReturn(page);
    }

    @State("Documents exist for the current user-owned search")
    public void documentsExistForOwnedSearch() {
        StoredDocument doc = createSampleStoredDocument();
        Page<StoredDocument> page = new PageImpl<>(List.of(doc));
        when(securityUtilService.getUserId()).thenReturn("test-user");
        when(searchService.findStoredDocumentsByCreator(any(), any())).thenReturn(page);
    }
}
