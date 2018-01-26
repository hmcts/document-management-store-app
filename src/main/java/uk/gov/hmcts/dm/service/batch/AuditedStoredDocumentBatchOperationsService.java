package uk.gov.hmcts.dm.service.batch;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

@Transactional
@Service
public class AuditedStoredDocumentBatchOperationsService {

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Autowired
    private AuditEntryService auditEntryService;

    @Value("${spring.batch.job.auditUserName}")
    @Getter
    private String batchAuditUserName;

    public void hardDeleteStoredDocument(StoredDocument storedDocument) {
        if (storedDocument != null && !storedDocument.isHardDeleted()) {
            storedDocumentService.deleteDocument(storedDocument, true);
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED, batchAuditUserName);
        }
    }

    public void hardDeleteAllExpiredStoredDocuments() {
        storedDocumentService
            .findAllExpiredStoredDocuments()
            .forEach( this::hardDeleteStoredDocument );

    }

}
