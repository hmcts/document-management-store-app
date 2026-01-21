package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.security.Classifications;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditedStoredDocumentOperationsServiceTests {

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private AuditEntryService auditEntryService;

    @InjectMocks
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    private final UUID docId = UUID.randomUUID();
    private final MockMultipartFile testFile = new MockMultipartFile(
        "file", "filename.txt", "text/plain", "content".getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void testReadStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.of(storedDocument));

        auditedStoredDocumentOperationsService.readStoredDocument(docId);

        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    void testReadNullStoredDocument() {
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.empty());

        StoredDocument readStoredDocument = auditedStoredDocumentOperationsService.readStoredDocument(docId);

        assertNull(readStoredDocument);
        verify(auditEntryService, never()).createAndSaveEntry(any(StoredDocument.class), any(AuditActions.class));
    }

    @Test
    void testAddDocumentVersion() {
        StoredDocument storedDocument = new StoredDocument();
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        String detectedMimeType = "text/plain";

        when(storedDocumentService.addStoredDocumentVersion(storedDocument, testFile, detectedMimeType))
            .thenReturn(documentContentVersion);

        auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, testFile, detectedMimeType);

        verify(storedDocumentService).addStoredDocumentVersion(storedDocument, testFile, detectedMimeType);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
        verify(auditEntryService).createAndSaveEntry(documentContentVersion, AuditActions.CREATED);
    }

    @Test
    void testCreateStoredDocuments() {
        UploadDocumentsCommand documentsCommand = new UploadDocumentsCommand();
        documentsCommand.setFiles(List.of(testFile));
        documentsCommand.setClassification(Classifications.PRIVATE);
        documentsCommand.setRoles(List.of("role1"));

        StoredDocument storedDocument = new StoredDocument();
        DocumentContentVersion dcv = new DocumentContentVersion();
        storedDocument.setDocumentContentVersions(List.of(dcv));
        List<StoredDocument> storedDocuments = List.of(storedDocument);

        Map<MultipartFile, String> mimeTypes = Map.of(testFile, "text/plain");

        when(storedDocumentService.saveItems(documentsCommand, mimeTypes)).thenReturn(storedDocuments);

        auditedStoredDocumentOperationsService.createStoredDocuments(documentsCommand, mimeTypes);

        verify(storedDocumentService).saveItems(documentsCommand, mimeTypes);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.CREATED);
        verify(auditEntryService).createAndSaveEntry(dcv, AuditActions.CREATED);
    }

    @Test
    void testDeleteStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.of(storedDocument));

        auditedStoredDocumentOperationsService.deleteStoredDocument(docId, false);

        verify(storedDocumentService).findOne(docId);
        verify(storedDocumentService).deleteDocument(storedDocument, false);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    void testDeleteDeletedStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.of(storedDocument));

        auditedStoredDocumentOperationsService.deleteStoredDocument(docId, false);

        verify(storedDocumentService).findOne(docId);
        verify(storedDocumentService, never()).deleteDocument(any(StoredDocument.class), any(Boolean.class));
        verify(auditEntryService, never()).createAndSaveEntry(any(StoredDocument.class), any(AuditActions.class));
    }

    @Test
    void testDeleteNullStoredDocument() {
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.empty());

        auditedStoredDocumentOperationsService.deleteStoredDocument(docId, false);

        verify(storedDocumentService).findOne(docId);
        verify(storedDocumentService, never()).deleteDocument(any(StoredDocument.class), any(Boolean.class));
        verify(auditEntryService, never()).createAndSaveEntry(any(StoredDocument.class), any(AuditActions.class));
    }

    @Test
    void testHardDeleteOnNotHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();

        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);

        verify(storedDocumentService).deleteDocument(storedDocument, true);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    void testHardDeleteOnHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);

        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);

        verify(storedDocumentService, never()).deleteDocument(any(StoredDocument.class), any(Boolean.class));
        verify(auditEntryService, never()).createAndSaveEntry(any(StoredDocument.class), any(AuditActions.class));
    }

    @Test
    void testHardDeleteOnSoftDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);

        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);

        verify(storedDocumentService).deleteDocument(storedDocument, true);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    void testUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.of(storedDocument));

        auditedStoredDocumentOperationsService.updateDocument(docId, command);

        verify(storedDocumentService).findOne(docId);
        verify(storedDocumentService).updateStoredDocument(storedDocument, command);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
    }

    @Test
    void testUpdateDocumentThatDoesNotExist() {
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(docId)).thenReturn(Optional.empty());

        assertThrows(StoredDocumentNotFoundException.class, () ->
            auditedStoredDocumentOperationsService.updateDocument(docId, command)
        );
    }

    @Test
    void testBulkUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(docId);
        Map<String, String> metadata = new HashMap<>(Map.of("UpdateKey", "UpdateValue"));
        Date newTtl = new Date();

        when(storedDocumentService.findOne(docId)).thenReturn(Optional.of(storedDocument));

        auditedStoredDocumentOperationsService.updateDocument(docId, metadata, newTtl);

        verify(storedDocumentService).findOne(docId);
        verify(storedDocumentService).updateStoredDocument(storedDocument, newTtl, metadata);
        verify(auditEntryService).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
    }

    @Test
    void testBulkUpdateDocumentWithError() {
        Map<String, String> metadata = Map.of("UpdateKey", "UpdateValue");
        Date newTtl = new Date();

        when(storedDocumentService.findOne(docId)).thenReturn(Optional.empty());

        assertThrows(StoredDocumentNotFoundException.class, () ->
            auditedStoredDocumentOperationsService.updateDocument(docId, metadata, newTtl)
        );
    }

    @Test
    void testDeleteCaseStoredDocumentsShouldMarkAllDocumentsForDeletion() {
        StoredDocument doc1 = new StoredDocument();
        doc1.setId(UUID.randomUUID());

        List<StoredDocument> storedDocuments = Collections.singletonList(doc1);

        final CaseDocumentsDeletionResults results =
            auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(storedDocuments);

        assertEquals(1, results.getCaseDocumentsFound());
        assertEquals(1, results.getMarkedForDeletion());

        verify(storedDocumentService).deleteDocument(doc1,false);
        verify(auditEntryService).createAndSaveEntry(doc1, AuditActions.DELETED);
        assertNotNull(doc1.getTtl());
    }
}
