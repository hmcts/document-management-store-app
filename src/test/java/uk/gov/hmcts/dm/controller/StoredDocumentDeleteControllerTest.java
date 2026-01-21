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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredDocumentDeleteControllerTest {

    @Mock
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Mock
    private SearchService searchService;

    @Mock
    private StoredDocument storedDocument;

    @InjectMocks
    private StoredDocumentDeleteController storedDocumentDeleteController;

    @Test
    void deleteDocumentShouldReturn204() {
        UUID documentId = UUID.randomUUID();
        boolean permanent = false;

        ResponseEntity<Object> response = storedDocumentDeleteController.deleteDocument(documentId, permanent);

        assertEquals(204, response.getStatusCode().value());
        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(documentId, permanent);
    }

    @Test
    void deleteDocumentShouldReturn204WhenPermanentIsTrue() {
        UUID documentId = UUID.randomUUID();
        boolean permanent = true;

        ResponseEntity<Object> response = storedDocumentDeleteController.deleteDocument(documentId, permanent);

        assertEquals(204, response.getStatusCode().value());
        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(documentId, permanent);
    }

    @Test
    void deleteCaseDocumentsShouldReturn200AndResults() {
        DeleteCaseDocumentsCommand command = new DeleteCaseDocumentsCommand("1234567812345678");
        List<StoredDocument> documents = List.of(storedDocument);
        CaseDocumentsDeletionResults results = new CaseDocumentsDeletionResults(1, 1);

        when(searchService.findStoredDocumentsIdsByCaseRef(command)).thenReturn(documents);
        when(auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(documents)).thenReturn(results);

        ResponseEntity<CaseDocumentsDeletionResults> response =
            storedDocumentDeleteController.deleteCaseDocuments(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(results, response.getBody());

        verify(searchService).findStoredDocumentsIdsByCaseRef(command);
        verify(auditedStoredDocumentOperationsService).deleteCaseStoredDocuments(documents);
    }

    @Test
    void deleteCaseDocumentsShouldReturn400OnInvalidRequest() {
        DeleteCaseDocumentsCommand command = new DeleteCaseDocumentsCommand("012");

        ResponseEntity<CaseDocumentsDeletionResults> response =
            storedDocumentDeleteController.deleteCaseDocuments(command);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
