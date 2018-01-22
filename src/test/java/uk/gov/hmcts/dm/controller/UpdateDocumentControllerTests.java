package uk.gov.hmcts.dm.controller;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateDocumentControllerTests extends ComponentTestBase {

    private final DocumentContent documentContent = new DocumentContent(new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8)));

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
        .documentContent(documentContent).build();

    private final StoredDocument storedDocument = StoredDocument.builder().id(id).ttl(new Date())
        .folder(Folder.builder().id(id).build()).documentContentVersions(
            Stream.of(documentContentVersion)
                .collect(Collectors.toList())
        ).build();

    public UpdateDocumentControllerTests() throws Exception {
    }

    @Test
    public void testUpdateDocument() throws Exception {

        when(this.auditedStoredDocumentOperationsService.updateDocument(eq(id), any(UpdateDocumentCommand.class)))
            .thenReturn(storedDocument);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .patch("/documents/" + id, ImmutableMap.of("ttl", new Date()))
            .andExpect(status().isOk());

    }


}
