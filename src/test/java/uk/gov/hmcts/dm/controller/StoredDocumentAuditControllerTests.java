package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by pawel on 26/05/2017.
 */
public class StoredDocumentAuditControllerTests extends ComponentTestBase {

    @Test
    public void testGetAuditEntries() throws Exception {

        when(this.storedDocumentService.findOne(TestUtil.RANDOM_UUID))
            .thenReturn(TestUtil.STORED_DOCUMENT);

        StoredDocumentAuditEntry entry = new StoredDocumentAuditEntry();
        entry.setStoredDocument(TestUtil.STORED_DOCUMENT);

        when(this.auditEntryService.findStoredDocumentAudits(TestUtil.STORED_DOCUMENT))
            .thenReturn(Stream.of(entry).collect(Collectors.toList()));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditEntriesOnDocumentThatDoesNotExist() throws Exception {

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + TestUtil.RANDOM_UUID + "/auditEntries")
            .andExpect(status().isNotFound());
    }

}
