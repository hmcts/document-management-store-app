package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by pawel on 12/02/2018.
 */
public class DocumentThumbnailControllerTests extends ComponentTestBase {

    private final DocumentContent documentContent = new DocumentContent(new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8)));

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
        .documentContent(documentContent).build();

    public DocumentThumbnailControllerTests() throws Exception {}

    @Test
    public void testGetDocumentThumbnail() {

        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/thumbnail");
    }

    @Test
    public void testGetDocumentVersionThumbnail() {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail");
    }

    @Test
    public void testGetDocumentVersionThumbnailThatStoredDocumentWasDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(new StoredDocument());
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }


    @Test
    public void testGetDocumentVersionThumbnailThatDoesntExist() throws Exception {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetThumbnail() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(), new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), "user", false);

        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            documentContentVersion
        );

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/thumbnail")
            .andExpect(status().isOk());
    }

    @Test
    public void testGetThumbnailThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }
}
