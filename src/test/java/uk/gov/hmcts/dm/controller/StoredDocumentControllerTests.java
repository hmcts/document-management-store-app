package uk.gov.hmcts.dm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoredDocumentControllerTests extends ComponentTestBase {

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
    public void testGetDocument() throws Exception {
        when(this.auditedStoredDocumentOperationsService.readStoredDocument(id))
            .thenReturn(storedDocument);
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentBinaryFromBlobStore() throws Exception {
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
    public void testGetDocumentBinaryWithChunkingFromBlobStore() throws Exception {
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
    public void testGetDocument404() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + UUID.randomUUID())
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateFromDocuments() throws Exception {
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
    public void testCreateFromDocumentsWithNonWhitelistFile() throws Exception {
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
    public void testGetBinary() throws Exception {

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
    public void testGetBinaryException() throws Exception {
        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        doThrow(UncheckedIOException.class).when(auditedDocumentContentVersionOperationsService)
                .readDocumentContentVersionBinaryFromBlobStore(Mockito.any(DocumentContentVersion.class),
                    Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }

    @Test
    public void testGetBinaryClientAbortException() throws Exception {
        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
        );

        doThrow(ClientAbortException.class).when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(Mockito.any(DocumentContentVersion.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk());
    }

    @Test
    public void testGetThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetBinaryThatDoesNotExist() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testDelete() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id)
            .andExpect(status().is(204));
        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
    }

    @Test
    public void testHardDelete() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id + "?permanent=true")
            .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, true);
    }

    @Test
    public void testSoftDeleteWithParam() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .delete("/documents/" + id + "?permanent=false")
            .andExpect(status().is(204));

        verify(auditedStoredDocumentOperationsService).deleteStoredDocument(id, false);
    }

    @Test
    public void testReturn400WhenUuidInvalid() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .get("/documents/123456")
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testInitBinder() {
        WebDataBinder webDataBinder = new WebDataBinder(null);

        Assert.assertNull(webDataBinder.getDisallowedFields());
        new StoredDocumentController(documentContentVersionService, auditedStoredDocumentOperationsService,
            auditedDocumentContentVersionOperationsService, toggleConfiguration).initBinder(webDataBinder);
        Assert.assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }

}
