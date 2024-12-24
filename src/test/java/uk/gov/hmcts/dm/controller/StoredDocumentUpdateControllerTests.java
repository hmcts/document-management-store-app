package uk.gov.hmcts.dm.controller;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentUpdateControllerTests extends ComponentTestBase {

    private final UUID id = UUID.randomUUID();

    private final StoredDocumentNotFoundException storedDocumentNotFoundException =
        new StoredDocumentNotFoundException(id);

    @Test
    void testUpdateDocument() throws Exception {

        when(this.auditedStoredDocumentOperationsService.updateDocument(eq(id), any(UpdateDocumentCommand.class)))
            .thenReturn(new StoredDocument());

        restActions
            .withAuthorizedUser("userId")
            .patch("/documents/" + id, Map.of("ttl", new Date()))
            .andExpect(status().isOk());

    }

    @Test
    void testBulkUpdate() throws Exception {

        Date ttl = new Date();
        when(this.auditedStoredDocumentOperationsService.updateDocument(eq(id), any(), eq(ttl)))
            .thenReturn(new StoredDocument());

        restActions
            .withAuthorizedUser("userId")
            .patch("/documents", Map.of(
                "ttl", ttl,
                "documents", Lists.newArrayList(
                    Map.of(
                        "documentId", id,
                        "metadata", Map.of("key", "value")
                    )
                )
            ))
            .andExpect(status().isOk());

    }

    @Test
    void testBulkUpdateStoredDocumentNotFoundException() throws Exception {

        Date ttl = new Date();

        doThrow(storedDocumentNotFoundException).when(this.auditedStoredDocumentOperationsService)
            .updateDocument(any(UUID.class),
                any(), any(Date.class));

        restActions
            .withAuthorizedUser("userId")
            .patch("/documents", Map.of(
                "ttl", ttl,
                "documents", Lists.newArrayList(
                    Map.of(
                        "documentId", UUID.randomUUID(),
                        "metadata", Map.of("key2", "value2")
                    )
                )
            ))
            .andExpect(status().isNotFound());
    }
}
