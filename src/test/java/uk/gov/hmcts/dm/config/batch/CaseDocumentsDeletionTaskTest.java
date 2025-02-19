package uk.gov.hmcts.dm.config.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    }

    @Test
    void shouldDeleteCaseDocumentsWhenScheduledTaskRuns() {
        caseDocumentsDeletionTask.execute();
        verify(storedDocumentService, times(1)).deleteCaseDocuments();
    }
}
