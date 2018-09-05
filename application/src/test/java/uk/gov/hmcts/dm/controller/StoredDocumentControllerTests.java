package uk.gov.hmcts.dm.controller;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
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
    public void testGetDocumentVersionBinaryThatIsDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        StoredDocument storedDocument = mock(StoredDocument.class);
        when(documentContentVersion.getStoredDocument()).thenReturn(storedDocument);
        when(storedDocument.isDeleted()).thenReturn(true);

        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary")
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
    public void testAddDocumentVersion() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.of(storedDocument));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class), any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    public void testAddDocumentToVersionToNotExistingOne() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testAddDocumentVersionWithNotAllowedFileType() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.of(storedDocument));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class), any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE_EXE)
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testCreateFromDocuments() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream().map(f -> new StoredDocument()).collect(Collectors.toList());

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);

        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(storedDocuments);

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

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);
        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetBinary() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(), new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), "user");

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
    public void testDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id)
                .andExpect(status().is(204));
        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
    }

    @Test
    public void testHardDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id + "?permanent=true")
                .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, true);
    }

    @Test
    public void testSoftDeleteWithParam() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id + "?permanent=false")
                .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
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
