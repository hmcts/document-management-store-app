package uk.gov.hmcts.dm.service.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.StoredDocument;

/**
 * Created by pawel on 24/01/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchStoredDocumentProcessorTests {

    @Mock
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @InjectMocks
    private BatchStoredDocumentProcessor service;

    @Test
    public void testProcess() throws Exception {
        StoredDocument storedDocument = new StoredDocument();
        service.process(storedDocument);
        Mockito.verify(auditedStoredDocumentBatchOperationsService, Mockito.times(1)).deleteStoredDocument(storedDocument);
    }

}
