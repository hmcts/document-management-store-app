package uk.gov.hmcts.dm.controller;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.rowset.serial.SerialBlob;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoredDocumentControllerTests extends ComponentTestBase {

    private final DocumentContent documentContent = new DocumentContent(new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8)));

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
        .documentContent(documentContent).build();

    private final StoredDocument storedDocument = StoredDocument.builder().id(id)
        .folder(Folder.builder().id(id).build()).documentContentVersions(
            Stream.of(documentContentVersion)
                .collect(Collectors.toList())
        ).build();

    public StoredDocumentControllerTests() throws Exception {
    }

    @Test
    public void testGetDocument() throws Exception {
        when(this.auditedStoredDocumentOperationsService.readStoredDocument(id))
            .thenReturn(storedDocument);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentBinary() {
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/binary");
    }

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
    public void testGetDocumentVersion() {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id);
    }

    @Test
    public void testGetDocumentVersionBinary() {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary");
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
    public void testGetDocumentVersionThatStoredDocumentWasDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(new StoredDocument());
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDocumentVersionBinaryThatStoredDocumentWasDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(new StoredDocument());
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
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
    public void testGetDocumentVersionThatDoesntExist() throws Exception {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDocumentVersionBinaryThatDoesntExist() throws Exception {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary")
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
    public void testGetDocument404() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + UUID.randomUUID())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateDocument() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(storedDocument);

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class), any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    public void updateDocumentDoesNotExist() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateFromDocuments() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream().map(f -> new StoredDocument()).collect(Collectors.toList());

        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(files)).thenReturn(storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().isOk());

    }

    @Test
    public void testCreateFromDocumentsWithNonWhitelistFile() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream().map(f -> new StoredDocument()).collect(Collectors.toList());

        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(files)).thenReturn(storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetBinary() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(), new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), null);

        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            documentContentVersion
        );

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }


    @Test
    public void testGetThumbnail() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(), new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), null);

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
    public void testGetThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetBinaryThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetThumbnailThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/thumbnail")
            .andExpect(status().isNotFound());
    }


    @Test
    public void testDelete() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .delete("/documents/" + id)
            .andExpect(status().is(405));
    }

    @Test
    @Ignore("for some reason does not work when moved to ErrorAttributes")
    public void testReturn404WhenUuidInvalid() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/123456")
            .andExpect(status().isNotFound());
    }

}
