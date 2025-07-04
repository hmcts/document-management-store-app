package uk.gov.hmcts.dm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.service.Constants;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentControllerTests extends ComponentTestBase {

    private final UUID id = UUID.randomUUID();

    private final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
        .id(id)
        .size(1L)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .storedDocument(StoredDocument.builder().id(id).build())
        .build();

    private final StoredDocument storedDocument = StoredDocument.builder().id(id)
        .documentContentVersions(
            Stream.of(documentContentVersion)
                .toList()
        ).build();

    @Test
    void testGetDocument() throws Exception {
        when(this.auditedStoredDocumentOperationsService.readStoredDocument(id))
            .thenReturn(storedDocument);
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id)
            .andExpect(status().isOk());
    }

    @Test
    void testGetDocumentBinaryFromBlobStore() throws Exception {
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType()))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString()))
            .andExpect(header().string("OriginalFileName", documentContentVersion.getOriginalDocumentName()))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName())));
    }

    @Test
    void testGetDocumentBinaryWithChunkingFromBlobStore() throws Exception {
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));
        when(this.toggleConfiguration.isChunking()).thenReturn(true);

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType()))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString()))
            .andExpect(header().string("OriginalFileName", documentContentVersion.getOriginalDocumentName()))
            .andExpect(header().string("Accept-Ranges", "bytes"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ACCEPT_RANGES))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName())));
    }

    @Test
    void testGetDocument404() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + UUID.randomUUID())
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFromDocuments() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream()
            .map(f -> new StoredDocument())
            .toList();

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);

        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(
            storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .postDocuments("/documents", files, Classifications.PUBLIC)
            .andExpect(status().isOk());

    }

    @Test
    void testCreateFromDocumentsWithNonWhitelistFile() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.exe", "", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream()
            .map(f -> new StoredDocument())
            .toList();

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);
        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(
            storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .postDocuments("/documents", files, Classifications.PUBLIC)
            .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetBinary() throws Exception {

        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }

    @Test
    void testGetBinaryException() throws Exception {
        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        doThrow(UncheckedIOException.class).when(auditedDocumentContentVersionOperationsService)
                .readDocumentContentVersionBinaryFromBlobStore(any(DocumentContentVersion.class),
                    any(HttpServletRequest.class), any(HttpServletResponse.class));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }

    @Test
    void testGetBinaryClientAbortException() throws Exception {
        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        doThrow(ClientAbortException.class).when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(DocumentContentVersion.class),
                any(HttpServletRequest.class), any(HttpServletResponse.class));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }

    @Test
    void testGetThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetBinaryThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id)
            .andExpect(status().is(204));
        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
    }

    @Test
    void testHardDelete() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id + "?permanent=true")
            .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, true);
    }

    @Test
    void testSoftDeleteWithParam() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id + "?permanent=false")
            .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
    }

    @Test
    void testReturn400WhenUuidInvalid() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/123456")
            .andExpect(status().isBadRequest());
    }

    @Test
    void testInitBinder() {
        WebDataBinder webDataBinder = new WebDataBinder(null);

        assertNull(webDataBinder.getDisallowedFields());
        new StoredDocumentController(documentContentVersionService, auditedStoredDocumentOperationsService,
            auditedDocumentContentVersionOperationsService, toggleConfiguration).initBinder(webDataBinder);
        assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }

    @Test
    void logsWarningWhenClientAbortExceptionOccurs() throws Exception {
        UncheckedIOException uncheckedIOException = new UncheckedIOException(new ClientAbortException("Broken pipe"));
        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
                .thenReturn(Optional.of(documentContentVersion));
        doThrow(uncheckedIOException).when(auditedDocumentContentVersionOperationsService)
                .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        restActions
                .withAuthorizedUser("userId")
                .get("/documents/" + id + "/binary")
                .andExpect(status().isOk());

    }
}
