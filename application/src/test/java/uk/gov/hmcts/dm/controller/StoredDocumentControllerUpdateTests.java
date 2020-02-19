package uk.gov.hmcts.dm.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoredDocumentControllerUpdateTests extends ComponentTestBase {

    private final UUID id = UUID.randomUUID();

    @Test
    public void testUpdateDocument() throws Exception {

        when(this.auditedStoredDocumentOperationsService.updateDocument(eq(id), any(UpdateDocumentCommand.class)))
            .thenReturn(new StoredDocument());

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .patch("/documents/" + id, ImmutableMap.of("ttl", new Date()))
            .andExpect(status().isOk());

    }

    @Test
    public void testBulkUpdate() throws Exception {

        when(this.auditedStoredDocumentOperationsService.updateDocuments(any(UpdateDocumentsCommand.class)))
            .thenReturn(ImmutableMap.of(id, "Success"));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .patch("/documents", ImmutableMap.of(
                "ttl", new Date(),
                "documents", Lists.newArrayList(
                    ImmutableMap.of(
                        "id", id,
                        "metadata", ImmutableMap.of("key", "value")
                    )
                )
            ))
            .andExpect(status().isOk());

    }

}
