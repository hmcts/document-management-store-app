package uk.gov.hmcts.dm.config.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;


@Component
public class DeleteExpiredDocumentsProcessor implements ItemProcessor<StoredDocument, StoredDocument> {

    private final AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    public DeleteExpiredDocumentsProcessor(
        AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService) {
        this.auditedStoredDocumentBatchOperationsService = auditedStoredDocumentBatchOperationsService;
    }

    @Override
    public StoredDocument process(StoredDocument storedDocument) {
        auditedStoredDocumentBatchOperationsService.hardDeleteStoredDocument(storedDocument);
        return storedDocument;
    }

}
