package uk.gov.hmcts.dm.controller;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.service.Constants;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
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
        .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
        .build();

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
    public void testGetDocumentBinaryFromBlobStore() throws Exception {
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));

        ResultActions result = restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/binary");

        String headerValue = header().toString();

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/" + id + "/binary")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType()))
            .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString()))
            .andExpect(header().string("OriginalFileName", documentContentVersion.getOriginalDocumentName()))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName())));
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
    public void testCreateFromDocuments() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream()
            .map(f -> new StoredDocument())
            .collect(Collectors.toList());

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);

        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(
            storedDocuments);

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
            new MockMultipartFile("files", "filename.exe", "", "hello2".getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());

        List<StoredDocument> storedDocuments = files.stream()
            .map(f -> new StoredDocument())
            .collect(Collectors.toList());

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(files);
        when(this.auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand)).thenReturn(
            storedDocuments);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetBinary() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(),
                                                                                   new MockMultipartFile("files",
                                                                                                         "filename.txt",
                                                                                                         "text/plain",
                                                                                                         "hello".getBytes(
                                                                                                             StandardCharsets.UTF_8)),
                                                                                   "user");

        documentContentVersion.setCreatedBy("userId");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id)).thenReturn(
            Optional.of(documentContentVersion)
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
    public void testReturn400WhenUuidInvalid() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/documents/123456")
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testInitBinder() {

        WebDataBinder webDataBinder = new WebDataBinder(null);

        Assert.assertNull(webDataBinder.getDisallowedFields());
        new StoredDocumentController().initBinder(webDataBinder);
        Assert.assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }

}
