package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.SearchService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
public class StoredDocumentDeleteControllerTest {

    @Mock
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private StoredDocumentDeleteController storedDocumentDeleteController;

    @Test
    void shouldReturnValidResponse() {
        final DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("0123456789123456");

        final List<StoredDocument> storedDocumentList = List.of(mock(StoredDocument.class));
        final CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults(1, 1);

        when(searchService.findStoredDocumentsIdsByCaseRef(deleteCaseDocumentsCommand)).thenReturn(storedDocumentList);
        when(auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(storedDocumentList)).thenReturn(caseDocumentsDeletionResults);

        final ResponseEntity<CaseDocumentsDeletionResults> caseDocumentsDeletionResultsResponseEntity =
                storedDocumentDeleteController.deleteCaseDocuments(deleteCaseDocumentsCommand);

        assertThat(caseDocumentsDeletionResultsResponseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Test
    void shouldReturnBadRequestResponse() {
        final DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("012");

        final ResponseEntity<CaseDocumentsDeletionResults> caseDocumentsDeletionResultsResponseEntity =
                storedDocumentDeleteController.deleteCaseDocuments(deleteCaseDocumentsCommand);

        assertThat(caseDocumentsDeletionResultsResponseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }
}
