package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentContentVersionControllerTest extends ComponentTestBase {

    @MockitoBean
    private FileContentVerifier fileContentVerifier;

    private final UUID id = UUID.randomUUID();
    private DocumentContentVersion documentContentVersion;
    private StoredDocument storedDocument;

    @BeforeEach
    void setup() {
        documentContentVersion = DocumentContentVersion.builder()
            .id(id)
            .size(1L)
            .mimeType("text/plain")
            .originalDocumentName("filename.txt")
            .storedDocument(StoredDocument.builder().id(id).build())
            .build();

        storedDocument = StoredDocument.builder().id(id)
            .documentContentVersions(List.of(documentContentVersion))
            .build();
    }


    @Test
    void testAddDocumentVersion() throws Exception {
        when(this.storedDocumentService.findOne(id)).thenReturn(Optional.of(storedDocument));

        final String detectedMimeType = "text/plain";
        when(fileContentVerifier.verifyContentType(any(MultipartFile.class)))
            .thenReturn(new FileVerificationResult(true, detectedMimeType));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class), eq(detectedMimeType)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .postDocumentVersion("/documents/" + id + "/versions", TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    void testAddDocumentVersionForVersionsMappingNotPresent() throws Exception {
        when(this.storedDocumentService.findOne(id)).thenReturn(Optional.of(storedDocument));

        final String detectedMimeType = "text/plain";
        when(fileContentVerifier.verifyContentType(any(MultipartFile.class)))
            .thenReturn(new FileVerificationResult(true, detectedMimeType));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class), eq(detectedMimeType)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    void testAddDocumentVersionWithNotAllowedFileType() throws Exception {
        when(this.storedDocumentService.findOne(id)).thenReturn(Optional.of(storedDocument));

        when(fileContentVerifier.verifyContentType(any(MultipartFile.class)))
            .thenReturn(new FileVerificationResult(false, "application/x-msdownload"));

        restActions
            .withAuthorizedUser("userId")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE_EXE)
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("Your upload contains a disallowed file type"));
    }

    @Test
    void testAddDocumentToVersionToNotExistingOne() throws Exception {
        when(this.storedDocumentService.findOne(id)).thenReturn(Optional.empty());

        when(fileContentVerifier.verifyContentType(any(MultipartFile.class)))
            .thenReturn(new FileVerificationResult(true, "text/plain"));

        restActions
            .withAuthorizedUser("userId")
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isNotFound());
    }


    @Test
    void testGetDocumentVersion() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isOk());
    }

    @Test
    void testGetDocumentVersionBinaryFromBlobStore() throws Exception {
        documentContentVersion.setContentUri("someURI");
        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1"))
            .andExpect(header().string("OriginalFileName", "filename.txt"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"filename.txt\""));
    }

    @Test
    void testGetDocumentVersionBinaryThatStoredDocumentWasDeleted() throws Exception {
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
    }


    @Test
    void testGetDocumentVersionThatDoesNotExist() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenThrow(new DocumentContentVersionNotFoundException(id));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetDocumentVersionBinaryThatDoesNotExist() throws Exception {
        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    void testInitBinder() {
        WebDataBinder webDataBinder = new WebDataBinder(null);

        assertNull(webDataBinder.getDisallowedFields());
        new DocumentContentVersionController(documentContentVersionService,
            auditedDocumentContentVersionOperationsService,
            storedDocumentService,
            auditedStoredDocumentOperationsService
        ).initBinder(webDataBinder);
        assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }

    @Test
    void returnsInternalServerErrorWhenIOExceptionOccurs() throws Exception {

        when(documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));
        doThrow(new IOException("Mocked IOException"))
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isInternalServerError());
    }
}
