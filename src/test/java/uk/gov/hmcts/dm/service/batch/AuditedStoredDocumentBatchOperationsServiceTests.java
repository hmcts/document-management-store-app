package uk.gov.hmcts.dm.service.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

/**
 * Created by pawel on 24/01/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditedStoredDocumentBatchOperationsServiceTests {

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private AuditEntryService auditEntryService;

    @InjectMocks
    private AuditedStoredDocumentBatchOperationsService service;

    @Test
    public void testDeleteNotDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        service.deleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(1)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(1))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, "(admin)");
    }

    @Test
    public void testDeleteSoftDeletedDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        service.deleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(1)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(1))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, "(admin)");
    }

    @Test
    public void testDeleteHardDeletedDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);
        service.deleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(0)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(0))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, "(admin)");
    }
}
