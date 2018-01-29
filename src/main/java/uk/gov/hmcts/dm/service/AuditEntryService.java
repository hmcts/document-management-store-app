package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.ServiceDetails;

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

    @Autowired
    private SecurityUtilService securityUtilService;

    public List<StoredDocumentAuditEntry> findStoredDocumentAudits(StoredDocument storedDocument) {
        return storedDocumentAuditEntryRepository.findByStoredDocumentOrderByRecordedDateTimeAsc(storedDocument);
    }

    public StoredDocumentAuditEntry createAndSaveEntry(StoredDocument storedDocument,
                                                       AuditActions action) {
        return createAndSaveEntry(storedDocument, action, securityUtilService.getCurrentlyAuthenticatedUsername());
    }

    public StoredDocumentAuditEntry createAndSaveEntry(StoredDocument storedDocument,
                                                       AuditActions action,
                                                       String username) {
        StoredDocumentAuditEntry storedDocumentAuditEntry = new StoredDocumentAuditEntry();
        populateCommonFields(storedDocumentAuditEntry, action, username);
        storedDocumentAuditEntry.setStoredDocument(storedDocument);
        storedDocumentAuditEntryRepository.save(storedDocumentAuditEntry);
        return storedDocumentAuditEntry;
    }

    public StoredDocumentAuditEntry createAndSaveEntry(DocumentContentVersion documentContentVersion,
                                                       AuditActions action) {
        return createAndSaveEntry(documentContentVersion, action, securityUtilService.getCurrentlyAuthenticatedUsername());
    }

    public DocumentContentVersionAuditEntry createAndSaveEntry(DocumentContentVersion documentContentVersion,
                                                               AuditActions action,
                                                               String username) {
        DocumentContentVersionAuditEntry documentContentVersionAuditEntry = new DocumentContentVersionAuditEntry();
        populateCommonFields(documentContentVersionAuditEntry, action, username);
        documentContentVersionAuditEntry.setDocumentContentVersion(documentContentVersion);
        documentContentVersionAuditEntry.setStoredDocument(documentContentVersion.getStoredDocument());
        documentContentVersionAuditEntryRepository.save(documentContentVersionAuditEntry);
        return documentContentVersionAuditEntry;
    }

    private void populateCommonFields(AuditEntry auditEntry, AuditActions action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String serviceName = null;
        if (authentication != null) {
            ServiceDetails userDetails = (ServiceDetails) authentication.getPrincipal();
            serviceName = userDetails.getUsername();
        }
        auditEntry.setAction(action);
        auditEntry.setServiceName(serviceName);
        auditEntry.setRecordedDateTime(new Date());
    }


}
