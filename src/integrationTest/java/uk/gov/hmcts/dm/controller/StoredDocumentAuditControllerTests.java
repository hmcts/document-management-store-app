package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentAuditControllerTests extends ComponentTestBase {

    @Test
    void testGetAuditEntries() throws Exception {
        when(this.storedDocumentRepository.findById(TestUtil.RANDOM_UUID))
                .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));

        StoredDocumentAuditEntry entry = new StoredDocumentAuditEntry();
        entry.setStoredDocument(TestUtil.STORED_DOCUMENT);

        when(this.auditEntryService.findStoredDocumentAudits(TestUtil.STORED_DOCUMENT))
                .thenReturn(Stream.of(entry).toList());

        restActions
                .withAuthorizedUser("userId")
                .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
                .andExpect(status().isOk());
    }

    @Test
    void testGetAuditEntriesOnDocumentThatDoesNotExist() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
                .andExpect(status().isNotFound());
    }

}
