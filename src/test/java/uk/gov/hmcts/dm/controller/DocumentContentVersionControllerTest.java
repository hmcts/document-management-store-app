package uk.gov.hmcts.dm.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentVersionCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.DocumentContentVersionHalResource;
import uk.gov.hmcts.dm.security.MultipartFileWhiteListValidator;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.FileVerificationResult;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentContentVersionControllerTest {

    @Mock
    private DocumentContentVersionService documentContentVersionService;
    @Mock
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;
    @Mock
    private StoredDocumentService storedDocumentService;
    @Mock
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @InjectMocks
    private DocumentContentVersionController controller;

    private UUID documentId;
    private UUID versionId;
    private StoredDocument storedDocument;
    private DocumentContentVersion documentContentVersion;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        request = new MockHttpServletRequest();

        storedDocument = new StoredDocument();
        storedDocument.setId(documentId);

        documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setId(versionId);
        documentContentVersion.setMimeType("text/plain");
        documentContentVersion.setOriginalDocumentName("test.txt");
        documentContentVersion.setSize(123L);
        documentContentVersion.setStoredDocument(storedDocument);
    }

    @Test
    void testInitBinder() {
        WebDataBinder binder = mock(WebDataBinder.class);
        controller.initBinder(binder);
        verify(binder).setDisallowedFields(Constants.IS_ADMIN);
    }

    @Test
    void testAddDocumentContentVersionSuccess() {
        MultipartFile file = mock(MultipartFile.class);
        UploadDocumentVersionCommand command = new UploadDocumentVersionCommand();
        command.setFile(file);
        FileVerificationResult result = new FileVerificationResult(true, "text/plain");
        request.setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, result);

        when(storedDocumentService.findOne(documentId)).thenReturn(Optional.of(storedDocument));
        when(auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, file, "text/plain"))
            .thenReturn(documentContentVersion);

        ResponseEntity<Object> response = controller.addDocumentContentVersion(documentId, command, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE, response.getHeaders().getContentType());
        assertInstanceOf(DocumentContentVersionHalResource.class, response.getBody());
        assertNotNull(((DocumentContentVersionHalResource) response.getBody()).getLink("self"));
    }

    @Test
    void testAddDocumentContentVersionLegacyMappingSuccess() {
        MultipartFile file = mock(MultipartFile.class);
        UploadDocumentVersionCommand command = new UploadDocumentVersionCommand();
        command.setFile(file);
        FileVerificationResult result = new FileVerificationResult(true, "text/plain");
        request.setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, result);

        when(storedDocumentService.findOne(documentId)).thenReturn(Optional.of(storedDocument));
        when(auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, file, "text/plain"))
            .thenReturn(documentContentVersion);

        ResponseEntity<Object> response = controller.addDocumentContentVersionForVersionsMappingNotPresent(documentId, command, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testAddDocumentContentVersionThrowsWhenVerificationResultIsNull() {
        request.setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, null);
        UploadDocumentVersionCommand command = new UploadDocumentVersionCommand();
        assertThrows(IllegalStateException.class,
            () -> controller.addDocumentContentVersion(documentId, command, request));
    }

    @Test
    void testAddDocumentContentVersionThrowsWhenMimeTypeIsMissing() {
        FileVerificationResult result = new FileVerificationResult(true, null); // MimeType is empty optional
        request.setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, result);
        UploadDocumentVersionCommand command = new UploadDocumentVersionCommand();
        assertThrows(IllegalStateException.class,
            () -> controller.addDocumentContentVersion(documentId, command, request));
    }

    @Test
    void testAddDocumentContentVersionThrowsWhenDocumentNotFound() {
        FileVerificationResult result = new FileVerificationResult(true, "text/plain");
        request.setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, result);
        when(storedDocumentService.findOne(documentId)).thenReturn(Optional.empty());

        UploadDocumentVersionCommand command = new UploadDocumentVersionCommand();
        assertThrows(StoredDocumentNotFoundException.class,
            () -> controller.addDocumentContentVersion(documentId, command, request));
    }

    @Test
    void testGetDocumentContentVersionDocumentSuccess() {
        when(auditedDocumentContentVersionOperationsService.readDocumentContentVersion(versionId))
            .thenReturn(documentContentVersion);

        ResponseEntity<Object> response = controller.getDocumentContentVersionDocument(documentId, versionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE, response.getHeaders().getContentType());
        assertInstanceOf(DocumentContentVersionHalResource.class, response.getBody());
    }

    @Test
    void testGetDocumentContentVersionDocumentBinarySuccess() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(documentContentVersionService.findById(versionId)).thenReturn(Optional.of(documentContentVersion));

        ResponseEntity<Object> result = controller.getDocumentContentVersionDocumentBinary(documentId, versionId, request, response);

        assertNull(result);

        verify(response).setHeader("Content-Type", "text/plain");
        verify(response).setHeader("Content-Length", "123");
        verify(response).setHeader("OriginalFileName", "test.txt");
        verify(response).setHeader("Content-Disposition", "fileName=\"test.txt\"");
        verify(response).setHeader("data-source", "contentURI");

        verify(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(documentContentVersion, request, response);

        verify(response).flushBuffer();
    }

    @Test
    void testGetBinaryThrowsWhenVersionNotFound() {
        when(documentContentVersionService.findById(versionId)).thenReturn(Optional.empty());

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(DocumentContentVersionNotFoundException.class,
            () -> controller.getDocumentContentVersionDocumentBinary(
                documentId, versionId, request, response
            )
        );
    }

    @Test
    void testGetBinaryThrowsWhenDocumentIsDeleted() {
        storedDocument.setDeleted(true);
        when(documentContentVersionService.findById(versionId)).thenReturn(Optional.of(documentContentVersion));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(DocumentContentVersionNotFoundException.class,
            () -> controller.getDocumentContentVersionDocumentBinary(
                documentId, versionId, request, response
            )
        );
    }

    @Test
    void testGetBinaryHandlesIoException() throws IOException {
        when(documentContentVersionService.findById(versionId)).thenReturn(Optional.of(documentContentVersion));
        IOException ioException = new IOException("Blob store failed");
        doThrow(ioException)
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        ResponseEntity<Object> response = controller.getDocumentContentVersionDocumentBinary(
            documentId, versionId, request, new MockHttpServletResponse()
        );

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ioException, response.getBody());
    }
}
