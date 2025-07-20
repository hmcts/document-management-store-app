package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentAuditControllerTests extends ComponentTestBase {

    @Test
    void testGetAuditEntries() throws Exception {
        when(this.storedDocumentRepository.findById(TestUtil.RANDOM_UUID))
            .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));

        StoredDocumentAuditEntry entry = new StoredDocumentAuditEntry();
        entry.setStoredDocument(TestUtil.STORED_DOCUMENT);
        entry.setAction(AuditActions.CREATED);
        entry.setUsername("test-user");

        when(this.auditEntryService.findStoredDocumentAudits(TestUtil.STORED_DOCUMENT))
            .thenReturn(Stream.of(entry).toList());

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.auditEntries[0].action").value("CREATED"))
            .andExpect(jsonPath("$._embedded.auditEntries[0].username").value("test-user"))
            .andExpect(jsonPath("$._embedded.auditEntries[0].type").value("StoredDocumentAuditEntry"))
            .andExpect(jsonPath("$._embedded.auditEntries[0]._links.document.href").exists())
            .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void testGetAuditEntriesOnDocumentThatDoesNotExist() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
                .andExpect(status().isNotFound());
    }

}
