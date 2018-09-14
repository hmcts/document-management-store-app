package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentContentVersionControllerTest extends ComponentTestBase {

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .size(1L)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
        .documentContent(new DocumentContent(serialBlob())).build();

    private final StoredDocument storedDocument = StoredDocument.builder().id(id)
        .folder(Folder.builder().id(id).build()).documentContentVersions(
            Stream.of(documentContentVersion)
                .collect(Collectors.toList())
        ).build();

    private static SerialBlob serialBlob() {
        try {
            return new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testAddDocumentVersion() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.of(storedDocument));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id + "/versions", TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    public void testAddDocumentVersionForVersionsMappingNotPresent() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.of(storedDocument));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class)))
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

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE_EXE)
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testGetDocumentVersion() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentVersionBinary() throws Exception {
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1"))
            .andExpect(header().string("OriginalFileName", "filename.txt"))
            .andExpect(header().string("data-source", "Postgres"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"filename.txt\""));
    }

    @Test
    public void testGetDocumentVersionBinaryFromBlobStore() throws Exception {
        documentContentVersion.setContentUri("someURI");
        when(this.documentContentVersionService.findOne(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1"))
            .andExpect(header().string("OriginalFileName", "filename.txt"))
            .andExpect(header().string("data-source", "contentURI"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"filename.txt\""));
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
    public void testGetDocumentVersionThatDoesNotExist() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenThrow(new DocumentContentVersionNotFoundException(id));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDocumentVersionBinaryThatDoesNotExist() throws Exception {
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

}
