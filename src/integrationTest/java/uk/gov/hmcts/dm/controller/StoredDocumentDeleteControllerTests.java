package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentDeleteControllerTests extends ComponentTestBase {

    @Test
    void testValidDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("0123456789123456");
        List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).toList();

        CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults();
        caseDocumentsDeletionResults.setCaseDocumentsFound(storedDocuments.size());
        caseDocumentsDeletionResults.setMarkedForDeletion(storedDocuments.size());

        when(
            this.searchService
                .findStoredDocumentsIdsByCaseRef(deleteCaseDocumentsCommand))
            .thenReturn(storedDocuments);

        when(
            this.auditedStoredDocumentOperationsService
                .deleteCaseStoredDocuments(storedDocuments))
            .thenReturn(caseDocumentsDeletionResults);

        restActions
            .withAuthorizedUser("userId")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().isOk());
    }

    @Test
    void testInValidDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("123");

        restActions
            .withAuthorizedUser("userId")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().is4xxClientError());
    }

    @Test
    void testEmptyDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand();

        restActions
            .withAuthorizedUser("userId")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().is4xxClientError());
    }

}
