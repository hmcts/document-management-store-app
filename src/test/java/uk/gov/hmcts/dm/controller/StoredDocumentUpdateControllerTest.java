package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentUpdateException;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;

import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredDocumentUpdateControllerTest {

    @Mock
    private AuditedStoredDocumentOperationsService documentService;

    @Mock
    private WebDataBinder webDataBinder;

    @Mock
    private StoredDocument storedDocument;

    @InjectMocks
    private StoredDocumentUpdateController controller;

    @Test
    void initBinderShouldDisallowIsAdminField() {
        controller.initBinder(webDataBinder);
        verify(webDataBinder).setDisallowedFields(Constants.IS_ADMIN);
    }

    @Test
    void updateDocumentShouldReturn200AndHalResource() {
        UUID documentId = UUID.randomUUID();
        UpdateDocumentCommand command = new UpdateDocumentCommand();

        when(documentService.updateDocument(documentId, command)).thenReturn(storedDocument);

        ResponseEntity<Object> response = controller.updateDocument(documentId, command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.includes(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE));
        assertInstanceOf(StoredDocumentHalResource.class, response.getBody());

        verify(documentService).updateDocument(documentId, command);
    }

    @Test
    void updateDocumentsShouldReturn200OnSuccess() {
        UUID docId = UUID.randomUUID();
        Map<String, String> metadata = Map.of("key", "value");
        Date ttl = new Date();

        DocumentUpdate docUpdate = new DocumentUpdate(docId, metadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(ttl, List.of(docUpdate));

        ResponseEntity<Object> response = controller.updateDocuments(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(new AbstractMap.SimpleEntry<>("result", "Success"), response.getBody());

        verify(documentService).updateDocument(docId, metadata, ttl);
    }

    @Test
    void updateDocumentsShouldThrowStoredDocumentNotFoundException() {
        UUID docId = UUID.randomUUID();
        Map<String, String> metadata = Map.of("key", "value");
        Date ttl = new Date();

        DocumentUpdate docUpdate = new DocumentUpdate(docId, metadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(ttl, List.of(docUpdate));

        doThrow(new StoredDocumentNotFoundException(docId))
            .when(documentService).updateDocument(docId, metadata, ttl);

        assertThrows(StoredDocumentNotFoundException.class, () -> controller.updateDocuments(command));

        verify(documentService).updateDocument(docId, metadata, ttl);
    }

    @Test
    void updateDocumentsShouldThrowDocumentUpdateExceptionOnGenericError() {
        UUID docId = UUID.randomUUID();
        Map<String, String> metadata = Map.of("key", "value");
        Date ttl = new Date();

        DocumentUpdate docUpdate = new DocumentUpdate(docId, metadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(ttl, List.of(docUpdate));

        doThrow(new RuntimeException("Generic error"))
            .when(documentService).updateDocument(docId, metadata, ttl);

        DocumentUpdateException exception = assertThrows(DocumentUpdateException.class,
            () -> controller.updateDocuments(command));

        assertEquals("Generic error", exception.getMessage());

        verify(documentService).updateDocument(docId, metadata, ttl);
    }
}
