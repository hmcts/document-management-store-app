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

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        service.hardDeleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(1)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(1))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, service.getBatchAuditUserName(), service.getBatchAuditServiceName());
    }

    @Test
    public void testDeleteSoftDeletedDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        service.hardDeleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(1)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(1))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, service.getBatchAuditUserName(), service.getBatchAuditServiceName());
    }

    @Test
    public void testDeleteHardDeletedDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);
        service.hardDeleteStoredDocument(storedDocument);
        Mockito.verify(storedDocumentService, Mockito.times(0)).deleteDocument(storedDocument, true);
        Mockito.verify(auditEntryService, Mockito.times(0))
            .createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, service.getBatchAuditUserName(), service.getBatchAuditServiceName());
    }

    @Test
    public void testDeleteAll() {
        StoredDocument storedDocument1 = new StoredDocument();
        storedDocument1.setHardDeleted(true);
        StoredDocument storedDocument2 = new StoredDocument();
        storedDocument2.setHardDeleted(false);
        Mockito.when(storedDocumentService.findAllExpiredStoredDocuments()).thenReturn(
            Stream.of(storedDocument1, storedDocument2).collect(Collectors.toList())
        );
        service.hardDeleteAllExpiredStoredDocuments();
        Mockito.verify(storedDocumentService, Mockito.times(1)).findAllExpiredStoredDocuments();
        Mockito.verify(storedDocumentService, Mockito.times(1)).deleteDocument(storedDocument2, true);
        Mockito.verify(auditEntryService, Mockito.times(1))
            .createAndSaveEntry(storedDocument2, AuditActions.HARD_DELETED, service.getBatchAuditUserName(), service.getBatchAuditServiceName());
    }
}
