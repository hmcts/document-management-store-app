package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.DocumentContentVersionAuditEntry;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEntryServiceTests {

    @Mock
    private StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository;

    @Mock
    private DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository;

    @Mock
    private SecurityUtilService securityUtilService;

    @InjectMocks
    private AuditEntryService auditEntryService;

    @Test
    void testCreateAndSaveEntryForStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(securityUtilService.getUserId()).thenReturn("user_x");
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("service_s");

        StoredDocumentAuditEntry entry = auditEntryService.createAndSaveEntry(storedDocument, AuditActions.READ);

        assertEquals("user_x", entry.getUsername());
        assertEquals("service_s", entry.getServiceName());
        assertEquals(AuditActions.READ, entry.getAction());
        assertEquals(storedDocument, entry.getStoredDocument());
        assertNotNull(entry.getRecordedDateTime());

        ArgumentCaptor<StoredDocumentAuditEntry> captor = ArgumentCaptor.forClass(StoredDocumentAuditEntry.class);
        verify(storedDocumentAuditEntryRepository).save(captor.capture());

        StoredDocumentAuditEntry savedEntry = captor.getValue();
        assertEquals("user_x", savedEntry.getUsername());
        assertEquals("service_s", savedEntry.getServiceName());
        assertEquals(AuditActions.READ, savedEntry.getAction());
    }

    @Test
    void testCreateAndSaveEntryForDocumentContentVersion() {
        StoredDocument storedDocument = new StoredDocument();
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(storedDocument);

        when(securityUtilService.getUserId()).thenReturn("user_y");
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("service_t");

        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.CREATED);

        ArgumentCaptor<DocumentContentVersionAuditEntry> captor =
            ArgumentCaptor.forClass(DocumentContentVersionAuditEntry.class);

        verify(documentContentVersionAuditEntryRepository).save(captor.capture());

        DocumentContentVersionAuditEntry savedEntry = captor.getValue();
        assertEquals("user_y", savedEntry.getUsername());
        assertEquals("service_t", savedEntry.getServiceName());
        assertEquals(AuditActions.CREATED, savedEntry.getAction());
        assertEquals(documentContentVersion, savedEntry.getDocumentContentVersion());
        assertEquals(storedDocument, savedEntry.getStoredDocument());
        assertNotNull(savedEntry.getRecordedDateTime());
    }

    @Test
    void testFindStoredDocumentAudits() {
        StoredDocument storedDocument = new StoredDocument();
        List<StoredDocumentAuditEntry> expectedList = List.of(new StoredDocumentAuditEntry());

        when(storedDocumentAuditEntryRepository.findByStoredDocumentOrderByRecordedDateTimeAsc(storedDocument))
            .thenReturn(expectedList);

        List<StoredDocumentAuditEntry> entries = auditEntryService.findStoredDocumentAudits(storedDocument);

        assertEquals(1, entries.size());
        assertEquals(expectedList, entries);
    }
}
