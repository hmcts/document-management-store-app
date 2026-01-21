package uk.gov.hmcts.dm.service.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditedStoredDocumentBatchOperationsServiceTests {

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private AuditEntryService auditEntryService;

    @InjectMocks
    private AuditedStoredDocumentBatchOperationsService service;

    private static final String TEST_USER = "batch_user";
    private static final String TEST_SERVICE = "batch_service";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "batchAuditUserName", TEST_USER);
        ReflectionTestUtils.setField(service, "batchAuditServiceName", TEST_SERVICE);
    }

    @Test
    void shouldHardDeleteDocumentWhenNotDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(false);
        storedDocument.setHardDeleted(false);

        service.hardDeleteStoredDocument(storedDocument);

        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(
            storedDocument,
            AuditActions.HARD_DELETED,
            TEST_USER,
            TEST_SERVICE
        );
    }

    @Test
    void shouldHardDeleteDocumentWhenSoftDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        storedDocument.setHardDeleted(false);

        service.hardDeleteStoredDocument(storedDocument);

        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(
            storedDocument,
            AuditActions.HARD_DELETED,
            TEST_USER,
            TEST_SERVICE
        );
    }

    @Test
    void shouldNotDeleteDocumentWhenAlreadyHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);

        service.hardDeleteStoredDocument(storedDocument);

        verify(storedDocumentService, never()).deleteDocument(storedDocument, true);
        verify(auditEntryService, never()).createAndSaveEntry(
            storedDocument,
            AuditActions.HARD_DELETED,
            TEST_USER,
            TEST_SERVICE
        );
    }

    @Test
    void shouldHardDeleteAllExpiredStoredDocuments() {
        StoredDocument storedDocument1 = new StoredDocument();
        storedDocument1.setHardDeleted(true);

        StoredDocument storedDocument2 = new StoredDocument();
        storedDocument2.setHardDeleted(false);

        when(storedDocumentService.findAllExpiredStoredDocuments())
            .thenReturn(List.of(storedDocument1, storedDocument2));

        service.hardDeleteAllExpiredStoredDocuments();

        verify(storedDocumentService, times(1)).findAllExpiredStoredDocuments();

        verify(storedDocumentService, never()).deleteDocument(storedDocument1, true);

        verify(storedDocumentService, times(1)).deleteDocument(storedDocument2, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(
            storedDocument2,
            AuditActions.HARD_DELETED,
            TEST_USER,
            TEST_SERVICE
        );
    }
}
