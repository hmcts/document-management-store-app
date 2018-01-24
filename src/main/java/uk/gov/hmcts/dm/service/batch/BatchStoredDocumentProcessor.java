package uk.gov.hmcts.dm.service.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.transaction.Transactional;

/**
 * Created by pawel on 24/01/2018.
 */
@Service
@Transactional
public class BatchStoredDocumentProcessor implements ItemProcessor<StoredDocument, StoredDocument> {

    @Autowired
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @Override
    public StoredDocument process(StoredDocument storedDocument) throws Exception {
        auditedStoredDocumentBatchOperationsService.deleteStoredDocument(storedDocument);
        return storedDocument;
    }
}
