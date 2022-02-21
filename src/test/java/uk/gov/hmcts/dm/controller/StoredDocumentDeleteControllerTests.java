package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoredDocumentDeleteControllerTests extends ComponentTestBase {

    @Test
    public void testValidDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("0123456789123456");
        List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).collect(Collectors.toList());

        CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults();
        caseDocumentsDeletionResults.setCaseDocumentsFound(storedDocuments.size());
        caseDocumentsDeletionResults.setMarkedForDeletion(storedDocuments.size());

        when(
            this.searchService
                .findStoredDocumentsByCaseRef(eq(deleteCaseDocumentsCommand)))
            .thenReturn(storedDocuments);

        when(
            this.auditedStoredDocumentOperationsService
                .deleteCaseStoredDocuments(eq(storedDocuments)))
            .thenReturn(caseDocumentsDeletionResults);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("ccd_data")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().isOk());
    }

    @Test
    public void testInValidDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand("123");

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("ccd_data")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyDeleteCaseDocumentsCommand() throws Exception {
        DeleteCaseDocumentsCommand deleteCaseDocumentsCommand = new DeleteCaseDocumentsCommand();

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("ccd_data")
            .post("/documents/delete", deleteCaseDocumentsCommand)
            .andExpect(status().is4xxClientError());
    }

}
