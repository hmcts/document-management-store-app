package uk.gov.hmcts.dm.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

@Slf4j
@Component
public class DeleteExpiredDocumentsProcessor implements ItemProcessor<StoredDocument, StoredDocument> {

    private final AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    public DeleteExpiredDocumentsProcessor(AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService) {
        this.auditedStoredDocumentBatchOperationsService = auditedStoredDocumentBatchOperationsService;
    }

    @Override
    public StoredDocument process(StoredDocument storedDocument) {
        log.info(
            "Deleting document blob {},doc count: {}, StoredDocument {},thread name {}",
            storedDocument.getDocumentContentVersions().get(0).getId(),
            storedDocument.getDocumentContentVersions().size(),
            storedDocument.getId(),
            Thread.currentThread().getName()
        );
        auditedStoredDocumentBatchOperationsService.hardDeleteStoredDocument(storedDocument);
        return storedDocument;
    }

}
