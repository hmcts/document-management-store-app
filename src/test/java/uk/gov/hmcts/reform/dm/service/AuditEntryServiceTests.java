package uk.gov.hmcts.reform.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.dm.componenttests.TestUtil;
import uk.gov.hmcts.reform.dm.domain.*;
import uk.gov.hmcts.reform.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.reform.dm.repository.StoredDocumentAuditEntryRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Created by pawel on 09/08/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditEntryServiceTests {

    @Mock
    private StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository;

    @Mock
    private DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository;

    @InjectMocks
    protected AuditEntryService auditEntryService;

    @Test
    public void testCreateAndSaveEntryForStoredDocument() {

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        ServiceAndUserDetails serviceAndUserDetails = mock(ServiceAndUserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getUsername()).thenReturn("x");

        SecurityContextHolder.setContext(securityContext);

        StoredDocument storedDocument = new StoredDocument();
        StoredDocumentAuditEntry entry = auditEntryService.createAndSaveEntry(storedDocument, AuditActions.READ);

        Assert.assertEquals("x", entry.getUsername());

        verify(storedDocumentAuditEntryRepository, times(1)).save(any(StoredDocumentAuditEntry.class));
    }

    @Test
    public void testCreateAndSaveEntryForDocumentContentVersion() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.CREATED);
        verify(documentContentVersionAuditEntryRepository, times(1)).save(any(DocumentContentVersionAuditEntry.class));
    }

    @Test
    public void testFindStoredDocumentAudits() {
        when(storedDocumentAuditEntryRepository
            .findByStoredDocumentOrderByRecordedDateTimeAsc(TestUtil.STORED_DOCUMENT))
            .thenReturn(Stream.of(new StoredDocumentAuditEntry()).collect(Collectors.toList()));
        List<StoredDocumentAuditEntry> entries = auditEntryService.findStoredDocumentAudits(TestUtil.STORED_DOCUMENT);
        Assert.assertEquals(1, entries.size());
    }

}
