package uk.gov.hmcts.dm.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteListValidator;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ExtendWith(MockitoExtension.class)
class StoredDocumentControllerTest {

    @Mock
    private DocumentContentVersionService documentContentVersionService;

    @Mock
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Mock
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Mock
    private ToggleConfiguration toggleConfiguration;

    @InjectMocks
    private StoredDocumentController storedDocumentController;

    private UUID documentId;
    private StoredDocument storedDocument;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        storedDocument = new StoredDocument();
        storedDocument.setId(documentId);
    }

    @Test
    void testInitBinder() {
        WebDataBinder binder = mock(WebDataBinder.class);
        storedDocumentController.initBinder(binder);
        verify(binder).setDisallowedFields(Constants.IS_ADMIN);
    }

    @Test
    void testCreateFromSuccess() {
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MultipartFile file = mock(MultipartFile.class);

        FileVerificationResult verificationResult = new FileVerificationResult(true, "text/plain");
        Map<MultipartFile, FileVerificationResult> verificationResultsMap = Map.of(file, verificationResult);
        request.setAttribute(MultipartFileListWhiteListValidator.VERIFICATION_RESULTS_MAP_KEY, verificationResultsMap);

        when(auditedStoredDocumentOperationsService.createStoredDocuments(eq(command), any(Map.class)))
            .thenReturn(List.of(storedDocument));

        ResponseEntity<Object> response = storedDocumentController.createFrom(command, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE, response.getHeaders().getContentType());
        assertInstanceOf(CollectionModel.class, response.getBody());

        CollectionModel<?> model = (CollectionModel<?>) response.getBody();
        assertNotNull(model);
        assertEquals(1, model.getContent().size());
        assertInstanceOf(StoredDocumentHalResource.class, model.getContent().iterator().next());
        verify(auditedStoredDocumentOperationsService).createStoredDocuments(eq(command), any(Map.class));
    }

    @Test
    void testCreateFromThrowsExceptionForNullVerificationResults() {
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        MockHttpServletRequest request = new MockHttpServletRequest();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> storedDocumentController.createFrom(command, request)
        );

        assertEquals("File verification results not found in request attributes.", exception.getMessage());
    }

    @Test
    void testCreateFromThrowsExceptionForMissingMimeType() {
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MultipartFile file = mock(MultipartFile.class);

        FileVerificationResult verificationResult = new FileVerificationResult(true, null);
        Map<MultipartFile, FileVerificationResult> verificationResultsMap = Map.of(file, verificationResult);
        request.setAttribute(MultipartFileListWhiteListValidator.VERIFICATION_RESULTS_MAP_KEY, verificationResultsMap);

        assertThrows(
            IllegalStateException.class,
            () -> storedDocumentController.createFrom(command, request)
        );
    }

    @Test
    void testGetMetaDataSuccess() {
        Map<String, String> headers = Map.of("header-key", "header-value");
        when(auditedStoredDocumentOperationsService.readStoredDocument(documentId)).thenReturn(storedDocument);

        ResponseEntity<Object> response = storedDocumentController.getMetaData(documentId, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertInstanceOf(StoredDocumentHalResource.class, response.getBody());

        StoredDocumentHalResource resource = (StoredDocumentHalResource) response.getBody();
        assertTrue(resource.getLink("self").isPresent());
        String expectedSelfLink = linkTo(methodOn(StoredDocumentController.class).getMetaData(documentId, null))
            .withSelfRel().getHref();
        Link self = resource.getLink("self").get();
        assertEquals(expectedSelfLink, self.getHref());
    }

    @Test
    void testGetMetaDataNotFound() {
        when(auditedStoredDocumentOperationsService.readStoredDocument(documentId)).thenReturn(null);

        ResponseEntity<Object> response = storedDocumentController.getMetaData(documentId, Collections.emptyMap());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetBinarySuccessChunkingDisabled() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, String> headers = Map.of("x-azure-ref", "some-ref");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(false);

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, headers, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("text/plain", response.getHeader(HttpHeaders.CONTENT_TYPE));
        assertEquals("123", response.getHeader(HttpHeaders.CONTENT_LENGTH));
        assertEquals("test.txt", response.getHeader("OriginalFileName"));
        assertEquals("fileName=\"test.txt\"", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        verify(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(dcv, request, response);
    }

    @Test
    void testGetBinarySuccessChunkingEnabled() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(true);

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, Collections.emptyMap(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("bytes", response.getHeader("Accept-Ranges"));
        assertEquals(HttpHeaders.ACCEPT_RANGES, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        verify(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(dcv, request, response);
    }

    @Test
    void testGetBinaryNotFound() {
        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.empty());

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Map<String, String> headers = Collections.emptyMap();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> storedDocumentController.getBinary(documentId, response, headers, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetBinaryHandlesIoExceptionWithChunking() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, String> headers = Map.of("header-key", "header-value");

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(true);
        doThrow(new IOException("Stream failed"))
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, headers, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(response).reset();
    }

    @Test
    void getBinaryShouldHandleIoExceptionWithoutChunking() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        MockHttpServletRequest request = new MockHttpServletRequest();

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(false);
        doThrow(new IOException("Stream failed"))
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, Collections.emptyMap(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(response, never()).reset();
    }

    @Test
    void testGetBinaryHandlesClientAbortException() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        MockHttpServletRequest request = new MockHttpServletRequest();

        UncheckedIOException uncheckedIoException = new UncheckedIOException(new ClientAbortException("Client closed"));

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(true);
        doThrow(uncheckedIoException)
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, Collections.emptyMap(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(response).reset();
    }

    @Test
    void testGetBinaryHandlesGenericUncheckedIoException() throws IOException {
        DocumentContentVersion dcv = createDocumentContentVersion();
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        MockHttpServletRequest request = new MockHttpServletRequest();

        UncheckedIOException uncheckedIoException = new UncheckedIOException(new IOException("Generic IO Error"));

        when(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId))
            .thenReturn(Optional.of(dcv));
        when(toggleConfiguration.isChunking()).thenReturn(true);
        doThrow(uncheckedIoException)
            .when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(any(), any(), any());

        ResponseEntity<Void> result = storedDocumentController
            .getBinary(documentId, response, Collections.emptyMap(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(response).reset();
    }

    private DocumentContentVersion createDocumentContentVersion() {
        DocumentContentVersion dcv = new DocumentContentVersion();
        dcv.setId(UUID.randomUUID());
        dcv.setMimeType("text/plain");
        dcv.setOriginalDocumentName("test.txt");
        dcv.setSize(123L);
        dcv.setStoredDocument(storedDocument);
        return dcv;
    }
}
