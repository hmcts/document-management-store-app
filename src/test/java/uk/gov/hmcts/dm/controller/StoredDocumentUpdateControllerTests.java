package uk.gov.hmcts.dm.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoredDocumentUpdateControllerTests extends ComponentTestBase {

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

        Date ttl = new Date();
        when(this.auditedStoredDocumentOperationsService.updateDocument(eq(id), any(), eq(ttl)))
            .thenReturn(new StoredDocument());

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .patch("/documents", ImmutableMap.of(
                "ttl", ttl,
                "documents", Lists.newArrayList(
                    ImmutableMap.of(
                        "documentId", id,
                        "metadata", ImmutableMap.of("key", "value")
                    )
                )
            ))
            .andExpect(status().isOk());

    }

    @Test
    public void testBulkUpdateException() throws Exception {

        Date ttl = new Date();
//        when(auditedStoredDocumentOperationsService.updateDocument(eq(id), any(), eq(ttl)))
//            .thenThrow(new StoredDocumentNotFoundException(id));
//        when(storedDocumentService.findOne(eq(id))).thenThrow(new StoredDocumentNotFoundException(id));
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .patch("/documents", ImmutableMap.of(
                "ttl", ttl,
                "documents", Lists.newArrayList(
                    ImmutableMap.of(
                        "documentId", UUID.randomUUID(),
                        "metadata", ImmutableMap.of("key2", "value2")
                    )
                )
            ))
            .andExpect(status().isNotFound());

    }

}
