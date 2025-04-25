package uk.gov.hmcts.dm.config.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CaseDocumentsDeletionTaskTest {

    @InjectMocks
    private CaseDocumentsDeletionTask caseDocumentsDeletionTask;

    @Mock
    private StoredDocumentService storedDocumentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "batchSize", 5);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "noOfIterations", 1);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "threadLimit", 1);
    }

    @Test
    void shouldDeleteCaseDocumentsWhenScheduledTaskRuns() {
        caseDocumentsDeletionTask.run();
        verify(storedDocumentService, times(1)).getAndDeleteCaseDocuments(0, 5, 1);
    }
}
