package uk.gov.hmcts.reform.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.dm.domain.*;
import uk.gov.hmcts.reform.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.reform.dm.repository.StoredDocumentAuditEntryRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by pawel on 24/07/2017.
 */
@Service
@Transactional
public class AuditEntryService {

    @Autowired
    private StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository;

    @Autowired
    private DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository;

    public List<StoredDocumentAuditEntry> findStoredDocumentAudits(StoredDocument storedDocument) {
        return storedDocumentAuditEntryRepository.findByStoredDocumentOrderByRecordedDateTimeAsc(storedDocument);
    }

    public StoredDocumentAuditEntry createAndSaveEntry(StoredDocument storedDocument, AuditActions action) {
        StoredDocumentAuditEntry storedDocumentAuditEntry = new StoredDocumentAuditEntry();
        populateCommonFields(storedDocumentAuditEntry, action);
        storedDocumentAuditEntry.setStoredDocument(storedDocument);
        storedDocumentAuditEntryRepository.save(storedDocumentAuditEntry);
        return storedDocumentAuditEntry;
    }

    public DocumentContentVersionAuditEntry createAndSaveEntry(DocumentContentVersion documentContentVersion, AuditActions action) {
        DocumentContentVersionAuditEntry documentContentVersionAuditEntry = new DocumentContentVersionAuditEntry();
        populateCommonFields(documentContentVersionAuditEntry, action);
        documentContentVersionAuditEntry.setDocumentContentVersion(documentContentVersion);
        documentContentVersionAuditEntry.setStoredDocument(documentContentVersion.getStoredDocument());
        documentContentVersionAuditEntryRepository.save(documentContentVersionAuditEntry);
        return documentContentVersionAuditEntry;
    }

    private void populateCommonFields(AuditEntry auditEntry, AuditActions action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null) {
            ServiceAndUserDetails userDetails = (ServiceAndUserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        }
        auditEntry.setAction(action);
        auditEntry.setUsername(username);
        auditEntry.setRecordedDateTime(new Date());
    }

}
