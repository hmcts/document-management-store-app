package uk.gov.hmcts.dm.service.batch;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

@Service
public class AuditedStoredDocumentBatchOperationsService {

    private final StoredDocumentService storedDocumentService;

    private final AuditEntryService auditEntryService;

    @Value("${spring.batch.job.auditUserName}")
    @Getter
    private String batchAuditUserName;

    @Value("${spring.batch.job.auditServiceName}")
    @Getter
    private String batchAuditServiceName;

    public AuditedStoredDocumentBatchOperationsService(StoredDocumentService storedDocumentService,
                                                       AuditEntryService auditEntryService) {
        this.storedDocumentService = storedDocumentService;
        this.auditEntryService = auditEntryService;
    }

    public void hardDeleteStoredDocument(StoredDocument storedDocument) {
        hardDeleteStoredDocument(storedDocument, batchAuditUserName, batchAuditServiceName);
    }

    public void hardDeleteStoredDocument(StoredDocument storedDocument, String userName, String serviceName) {
        if (storedDocument != null && !storedDocument.isHardDeleted()) {
            storedDocumentService.deleteDocument(storedDocument, true);
            auditEntryService.createAndSaveEntry(
                storedDocument,
                AuditActions.HARD_DELETED,
                userName,
                serviceName);
        }
    }

    public void hardDeleteAllExpiredStoredDocuments() {
        storedDocumentService
            .findAllExpiredStoredDocuments()
            .forEach(this::hardDeleteStoredDocument);

    }

}
