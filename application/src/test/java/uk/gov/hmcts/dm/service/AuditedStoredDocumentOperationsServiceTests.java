package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by pawel on 09/08/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditedStoredDocumentOperationsServiceTests {

    @Mock
    private StoredDocumentService storedDocumentService;

    @Mock
    private AuditEntryService auditEntryService;

    @InjectMocks
    AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;


    @Test
    public void testReadStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    public void testReadNullStoredDocument() {
        StoredDocument storedDocument = null;
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        StoredDocument readStoredDocument = auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        Assert.assertNull(readStoredDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    public void testAddDocumentVersion() {
        StoredDocument storedDocument = new StoredDocument();
        MultipartFile multipartFile = TestUtil.TEST_FILE;
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        when(storedDocumentService.addStoredDocumentVersion(storedDocument, multipartFile)).thenReturn(documentContentVersion);
        auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, multipartFile);
        verify(storedDocumentService, times(1)).addStoredDocumentVersion(storedDocument, multipartFile);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.CREATED);
    }

    @Test
    public void testCreateStoredDocuments() {
        List<MultipartFile> multipartFiles = Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList());
        UploadDocumentsCommand documentsCommand = new UploadDocumentsCommand();
        documentsCommand.setFiles(multipartFiles);
        documentsCommand.setClassification(Classifications.PRIVATE);
        documentsCommand.setRoles(Arrays.asList("role1"));

        List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).collect(Collectors.toList());
        when(storedDocumentService.saveItems(documentsCommand)).thenReturn(storedDocuments);
        auditedStoredDocumentOperationsService.createStoredDocuments(documentsCommand);
        verify(storedDocumentService, times(1)).saveItems(documentsCommand);
        verify(auditEntryService, times(1)).createAndSaveEntry(TestUtil.STORED_DOCUMENT, AuditActions.CREATED);
        verify(auditEntryService, times(1)).createAndSaveEntry(TestUtil.STORED_DOCUMENT.getDocumentContentVersions().get(0), AuditActions.CREATED);
    }

    @Test
    public void testDeleteStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    public void testDeleteDeletedStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    public void testDeleteNullStoredDocument() {
        StoredDocument storedDocument = null;
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID, false);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, false);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    public void testHardDeleteOnNotHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    public void testHardDeleteOnHardDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setHardDeleted(true);
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(0)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    public void testHardDeleteOnSoftDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        auditedStoredDocumentOperationsService.deleteStoredDocument(storedDocument, true);
        verify(storedDocumentService, times(1)).deleteDocument(storedDocument, true);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
    }

    @Test
    public void testUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(storedDocument));
        auditedStoredDocumentOperationsService.updateDocument(TestUtil.RANDOM_UUID, command);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).updateStoredDocument(storedDocument, command);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.UPDATED);
    }

    @Test(expected = StoredDocumentNotFoundException.class)
    public void testUpdateDocumentThatDoesNotExist() {
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(Optional.empty());
        auditedStoredDocumentOperationsService.updateDocument(TestUtil.RANDOM_UUID, command);
    }
}

