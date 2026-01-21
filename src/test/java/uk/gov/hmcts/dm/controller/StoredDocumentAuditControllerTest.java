package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.StoredDocumentAuditEntryHalResource;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredDocumentAuditControllerTest {

    @Mock
    private AuditEntryService auditEntryService;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @Mock
    private StoredDocument storedDocument;

    @Mock
    private StoredDocumentAuditEntry auditEntry;

    @InjectMocks
    private StoredDocumentAuditController storedDocumentAuditController;

    @Test
    void findAuditsShouldReturn200AndCollectionWhenDocumentFound() {
        UUID documentId = UUID.randomUUID();
        when(storedDocumentRepository.findById(documentId)).thenReturn(Optional.of(storedDocument));
        when(auditEntryService.findStoredDocumentAudits(storedDocument)).thenReturn(List.of(auditEntry));
        when(auditEntry.getStoredDocument()).thenReturn(storedDocument);


        ResponseEntity<CollectionModel<StoredDocumentAuditEntryHalResource>> response =
            storedDocumentAuditController.findAudits(documentId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.includes(V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE));
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        verify(storedDocumentRepository).findById(documentId);
        verify(auditEntryService).findStoredDocumentAudits(storedDocument);
    }

    @Test
    void findAuditsShouldReturn200AndEmptyCollectionWhenDocumentFoundButNoAudits() {
        UUID documentId = UUID.randomUUID();
        when(storedDocumentRepository.findById(documentId)).thenReturn(Optional.of(storedDocument));
        when(auditEntryService.findStoredDocumentAudits(storedDocument)).thenReturn(Collections.emptyList());

        ResponseEntity<CollectionModel<StoredDocumentAuditEntryHalResource>> response =
            storedDocumentAuditController.findAudits(documentId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());

        verify(storedDocumentRepository).findById(documentId);
        verify(auditEntryService).findStoredDocumentAudits(storedDocument);
    }

    @Test
    void findAuditsShouldThrowStoredDocumentNotFoundExceptionWhenDocumentNotFound() {
        UUID documentId = UUID.randomUUID();
        when(storedDocumentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(StoredDocumentNotFoundException.class,
            () -> storedDocumentAuditController.findAudits(documentId));

        verify(storedDocumentRepository).findById(documentId);
        verifyNoInteractions(auditEntryService);
    }
}
