package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentThumbnailControllerTests extends ComponentTestBase {

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).build())
        .build();

    public DocumentThumbnailControllerTests() throws Exception {
    }

    @Test
    public void testGetDocumentThumbnail() {

        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/thumbnail");
    }

    @Test
    public void testGetDocumentVersionThumbnail() {
        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail");
    }

    @Test
    public void testGetDocumentVersionThumbnailThatStoredDocumentWasDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(new StoredDocument());
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }


    @Test
    public void testGetDocumentVersionThumbnailThatDoesntExist() throws Exception {
        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetThumbnail() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(),
            new MockMultipartFile("files", "filename.txt",
                "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), "user");

        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/thumbnail")
            .andExpect(status().isOk());
    }

    @Test
    public void testGetThumbnailThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }
}
