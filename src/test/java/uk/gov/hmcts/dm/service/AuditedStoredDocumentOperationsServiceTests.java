package uk.gov.hmcts.dm.service;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AuditedStoredDocumentOperationsServiceTests {

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private AuditEntryService auditEntryService;

    @InjectMocks
    AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;


    @Test
    void testReadStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    void testReadNullStoredDocument() {
        StoredDocument storedDocument = null;
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        StoredDocument readStoredDocument =
            auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        assertNull(readStoredDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    void testAddDocumentVersion() {
        StoredDocument storedDocument = new StoredDocument();
        MultipartFile multipartFile = TestUtil.TEST_FILE;
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        when(storedDocumentService.addStoredDocumentVersion(storedDocument, multipartFile))
            .thenReturn(documentContentVersion);
        auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, multipartFile);
        verify(storedDocumentService, times(1)).addStoredDocumentVersion(storedDocument, multipartFile);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.CREATED);
    }

    @Test
    void testCreateStoredDocuments() {
        List<MultipartFile> multipartFiles = Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList());
        UploadDocumentsCommand documentsCommand = new UploadDocumentsCommand();
        documentsCommand.setFiles(multipartFiles);
        documentsCommand.setClassification(Classifications.PRIVATE);
        documentsCommand.setRoles(List.of("role1"));

        List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).toList();
        when(storedDocumentService.saveItems(documentsCommand)).thenReturn(storedDocuments);
        auditedStoredDocumentOperationsService.createStoredDocuments(documentsCommand);
        verify(storedDocumentService, times(1)).saveItems(documentsCommand);
        verify(auditEntryService, times(1)).createAndSaveEntry(TestUtil.STORED_DOCUMENT, AuditActions.CREATED);
        verify(auditEntryService, times(1)).createAndSaveEntry(
            TestUtil.STORED_DOCUMENT.getDocumentContentVersions().get(0), AuditActions.CREATED);
    }

    @Test
    void testDeleteStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    void testDeleteDeletedStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    void testDeleteNullStoredDocument() {
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        StoredDocument storedDocument = null;
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    void testHardDeleteOnNotHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    void testHardDeleteOnHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    void testHardDeleteOnSoftDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    void testUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.updateDocument(TestUtil.RANDOM_UUID, command);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).updateStoredDocument(storedDocument, command);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
    }

    @Test
    void testUpdateDocumentThatDoesNotExist() {
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        assertThrows(StoredDocumentNotFoundException.class, () -> {
            auditedStoredDocumentOperationsService.updateDocument(TestUtil.RANDOM_UUID, command);
        });
    }

    @Test
    void testBulkUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        Map<String, String> metadata = Maps.newHashMap("UpdateKey", "UpdateValue");

        Date newTtl = new Date();

        when(storedDocumentService.findOne(storedDocument.getId())).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.updateDocument(storedDocument.getId(), metadata, newTtl);
        verify(storedDocumentService, times(1)).findOne(storedDocument.getId());
        verify(storedDocumentService, times(1)).updateStoredDocument(storedDocument, newTtl, metadata);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
    }

    @Test
    void testBulkUpdateDocumentWithError() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        Map<String, String> metadata = Map.of("UpdateKey", "UpdateValue");
        Date newTtl = new Date();

        when(storedDocumentService.findOne(storedDocument.getId())).thenReturn(Optional.empty());
        var storedDocumentId = storedDocument.getId();
        assertThrows(StoredDocumentNotFoundException.class, () ->
            auditedStoredDocumentOperationsService.updateDocument(storedDocumentId, metadata, newTtl)
        );
    }

    @Test
    void testDeleteCaseStoredDocumentsShouldMarkAllDocumentsForDeletion() {
        final List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).toList();

        final CaseDocumentsDeletionResults caseDocumentsDeletionResults =
            auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(storedDocuments);

        assertThat(caseDocumentsDeletionResults.getCaseDocumentsFound().equals(storedDocuments.size()));
        assertThat(caseDocumentsDeletionResults.getMarkedForDeletion().equals(storedDocuments.size()));
    }
}

