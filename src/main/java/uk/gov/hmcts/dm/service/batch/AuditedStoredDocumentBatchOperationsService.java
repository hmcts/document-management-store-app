package uk.gov.hmcts.dm.service.batch;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

@Service
public class AuditedStoredDocumentBatchOperationsService {

    private static final Logger log = LoggerFactory.getLogger(AuditedStoredDocumentBatchOperationsService.class);

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Autowired
    private AuditEntryService auditEntryService;

    @Value("${spring.batch.job.auditUserName}")
    @Getter
    private String batchAuditUserName;

    @Value("${spring.batch.job.auditServiceName}")
    @Getter
    private String batchAuditServiceName;

    public void hardDeleteStoredDocument(StoredDocument storedDocument) {
        if (storedDocument != null && !storedDocument.isHardDeleted()) {
            log.info("Audited hardDeleteStoredDocument {}", storedDocument.getId());
            storedDocumentService.deleteDocument(storedDocument, true);
            auditEntryService.createAndSaveEntry(
                storedDocument,
                AuditActions.HARD_DELETED,
                batchAuditUserName,
                batchAuditServiceName);
        }
    }

    public void hardDeleteAllExpiredStoredDocuments() {
        storedDocumentService
            .findAllExpiredStoredDocuments()
            .forEach(this::hardDeleteStoredDocument);

    }

}
