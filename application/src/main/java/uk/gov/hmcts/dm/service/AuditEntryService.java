package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;

import java.util.Date;
import java.util.List;

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
        return createAndSaveEntry(
            storedDocument,
            action,
            securityUtilService.getUserId(),
            securityUtilService.getCurrentlyAuthenticatedServiceName());
    }

    public StoredDocumentAuditEntry createAndSaveEntry(StoredDocument storedDocument,
                                                       AuditActions action,
                                                       String username,
                                                       String serviceName) {
        StoredDocumentAuditEntry storedDocumentAuditEntry = new StoredDocumentAuditEntry();
        populateCommonFields(storedDocumentAuditEntry, action, username, serviceName);
        storedDocumentAuditEntry.setStoredDocument(storedDocument);
        storedDocumentAuditEntryRepository.save(storedDocumentAuditEntry);
        return storedDocumentAuditEntry;
    }

    public StoredDocumentAuditEntry createAndSaveEntry(DocumentContentVersion documentContentVersion,
                                                       AuditActions action) {
        return createAndSaveEntry(documentContentVersion, action,
                                  securityUtilService.getUserId(),
                                  securityUtilService.getCurrentlyAuthenticatedServiceName()
                                 );
    }

    public DocumentContentVersionAuditEntry createAndSaveEntry(DocumentContentVersion documentContentVersion,
                                                               AuditActions action,
                                                               String username, String serviceName) {
        DocumentContentVersionAuditEntry documentContentVersionAuditEntry = new DocumentContentVersionAuditEntry();
        populateCommonFields(documentContentVersionAuditEntry, action, username, serviceName);
        documentContentVersionAuditEntry.setDocumentContentVersion(documentContentVersion);
        documentContentVersionAuditEntry.setStoredDocument(documentContentVersion.getStoredDocument());
        documentContentVersionAuditEntryRepository.save(documentContentVersionAuditEntry);
        return documentContentVersionAuditEntry;
    }

    private void populateCommonFields(AuditEntry auditEntry,
                                      AuditActions action,
                                      String username,
                                      String serviceName) {
        auditEntry.setAction(action);
        auditEntry.setUsername(username);
        auditEntry.setServiceName(serviceName);
        auditEntry.setRecordedDateTime(new Date());
    }


}
