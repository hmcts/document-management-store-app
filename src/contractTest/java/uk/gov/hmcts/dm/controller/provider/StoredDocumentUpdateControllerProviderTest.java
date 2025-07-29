package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.Date;
import java.util.Map;
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
}
