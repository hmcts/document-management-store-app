package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.DocumentContentVersionAuditEntry;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AuditEntryServiceTests {

    @Mock
    private StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository;

    @Mock
    private DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository;

    @Mock
    private SecurityUtilService securityUtilService;

    @InjectMocks
    AuditEntryService auditEntryService;

    @Test
    public void testCreateAndSaveEntryForStoredDocument() {

        when(securityUtilService.getUserId()).thenReturn("x");
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("s");

        StoredDocumentAuditEntry entry = auditEntryService.createAndSaveEntry(new StoredDocument(), AuditActions.READ);

        Assert.assertEquals("x", entry.getUsername());
        Assert.assertEquals("s", entry.getServiceName());

        verify(storedDocumentAuditEntryRepository, times(1)).save(any(StoredDocumentAuditEntry.class));
    }

    @Test
    public void testCreateAndSaveEntryForDocumentContentVersion() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        when(securityUtilService.getUserId()).thenReturn("x");
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("s");
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.CREATED);
        verify(documentContentVersionAuditEntryRepository, times(1)).save(any(DocumentContentVersionAuditEntry.class));
    }

    @Test
    public void testFindStoredDocumentAudits() {
        when(storedDocumentAuditEntryRepository
                .findByStoredDocumentOrderByRecordedDateTimeAsc(TestUtil.STORED_DOCUMENT))
                .thenReturn(Stream.of(new StoredDocumentAuditEntry()).toList());
        List<StoredDocumentAuditEntry> entries = auditEntryService.findStoredDocumentAudits(TestUtil.STORED_DOCUMENT);
        Assert.assertEquals(1, entries.size());
    }


}
