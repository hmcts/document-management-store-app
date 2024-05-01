package uk.gov.hmcts.dm.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.Constants;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentContentVersionControllerTest extends ComponentTestBase {

    @Mock
    private WebDataBinder binder;

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
                .collect(Collectors.toList())
        ).build();

    @Test
    public void testAddDocumentVersion() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.of(storedDocument));

        when(this.auditedStoredDocumentOperationsService.addDocumentVersion(any(StoredDocument.class),
            any(MultipartFile.class)))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
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
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE)
            .andExpect(status().isCreated());
    }

    @Test
    public void testAddDocumentToVersionToNotExistingOne() throws Exception {
        when(this.storedDocumentService.findOne(id))
            .thenReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
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
            .postDocumentVersion("/documents/" + id, TestUtil.TEST_FILE_EXE)
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testGetDocumentVersion() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenReturn(documentContentVersion);

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentVersionBinaryFromBlobStore() throws Exception {
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
    public void testGetDocumentVersionBinaryThatStoredDocumentWasDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(new StoredDocument());
        documentContentVersion.getStoredDocument().setDeleted(true);

        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
    }


    @Test
    public void testGetDocumentVersionThatDoesNotExist() throws Exception {
        when(this.auditedDocumentContentVersionOperationsService.readDocumentContentVersion(id))
            .thenThrow(new DocumentContentVersionNotFoundException(id));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDocumentVersionBinaryThatDoesNotExist() throws Exception {
        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDocumentVersionBinaryThatIsDeleted() throws Exception {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        StoredDocument storedDocument = mock(StoredDocument.class);
        when(documentContentVersion.getStoredDocument()).thenReturn(storedDocument);
        when(storedDocument.isDeleted()).thenReturn(true);

        when(this.documentContentVersionService.findById(id))
            .thenReturn(Optional.of(documentContentVersion));

        restActions
            .withAuthorizedUser("userId")
            .get("/documents/" + id + "/versions/" + id + "/binary")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testInitBinder() {
        WebDataBinder webDataBinder = new WebDataBinder(null);

        Assert.assertNull(webDataBinder.getDisallowedFields());
        new DocumentContentVersionController(documentContentVersionService,
            auditedDocumentContentVersionOperationsService,
            storedDocumentService,
            auditedStoredDocumentOperationsService
        ).initBinder(webDataBinder);
        Assert.assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }
}
