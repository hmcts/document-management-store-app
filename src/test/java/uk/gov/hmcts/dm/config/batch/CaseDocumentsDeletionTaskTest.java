package uk.gov.hmcts.dm.config.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CaseDocumentsDeletionTaskTest {

    @InjectMocks
    private CaseDocumentsDeletionTask caseDocumentsDeletionTask;

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "batchSize", 5);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "noOfIterations", 1);
        ReflectionTestUtils.setField(caseDocumentsDeletionTask, "threadLimit", 1);
    }

    @Test
    void shouldLogErrorWhenDeletionJobFails() {
        doThrow(new RuntimeException("Simulated failure"))
                .when(storedDocumentService).deleteDocumentsDetails(anyList());

        caseDocumentsDeletionTask.run();

        verify(storedDocumentService, times(0)).deleteDocumentsDetails(anyList());

    }

    @Test
    void shouldNotProcessWhenNoDocumentsAreFound() {
        when(storedDocumentRepository.findCaseDocumentsForDeletion(any())).thenReturn(List.of());

        caseDocumentsDeletionTask.run();

        verify(storedDocumentService, times(0)).deleteDocumentsDetails(anyList());
    }

    @Test
    void shouldProcessDocumentsInBatches() {
        StoredDocument document1 = new StoredDocument();
        StoredDocument document2 = new StoredDocument();
        when(storedDocumentRepository.findCaseDocumentsForDeletion(any())).thenReturn(List.of(document1, document2));

        caseDocumentsDeletionTask.run();

        verify(storedDocumentService, times(1)).deleteDocumentsDetails(anyList());
    }
}
