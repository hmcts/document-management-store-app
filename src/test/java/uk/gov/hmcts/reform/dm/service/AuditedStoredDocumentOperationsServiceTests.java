package uk.gov.hmcts.reform.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.dm.componenttests.TestUtil;
import uk.gov.hmcts.reform.dm.domain.AuditActions;
import uk.gov.hmcts.reform.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.reform.dm.domain.StoredDocument;
import uk.gov.hmcts.reform.dm.security.Classifications;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

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
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;


    @Test
    public void testReadStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }


    @Test
    public void testReadDeletedStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        StoredDocument readStoredDocument = auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        Assert.assertNull(readStoredDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }


    @Test
    public void testReadNullStoredDocument() {
        StoredDocument storedDocument = null;
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        StoredDocument readStoredDocument = auditedStoredDocumentOperationsService.readStoredDocument(TestUtil.RANDOM_UUID);
        Assert.assertNull(readStoredDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.READ);
    }

    @Test
    public void testCreateStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        auditedStoredDocumentOperationsService.createStoredDocument(storedDocument);
        verify(storedDocumentService, times(1)).save(storedDocument);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.CREATED);
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
        List<StoredDocument> storedDocuments = Stream.of(TestUtil.STORED_DOCUMENT).collect(Collectors.toList());
        when(storedDocumentService.saveItems(multipartFiles, Classifications.PRIVATE, Arrays.asList("role1"), null)).thenReturn(storedDocuments);
        auditedStoredDocumentOperationsService.createStoredDocuments(multipartFiles, Classifications.PRIVATE, Arrays.asList("role1"), null);
        verify(storedDocumentService, times(1)).saveItems(multipartFiles, Classifications.PRIVATE, Arrays.asList("role1"), null);
        verify(auditEntryService, times(1)).createAndSaveEntry(TestUtil.STORED_DOCUMENT, AuditActions.CREATED);
        verify(auditEntryService, times(1)).createAndSaveEntry(TestUtil.STORED_DOCUMENT.getDocumentContentVersions().get(0), AuditActions.CREATED);
    }

    @Test
    public void testDeleteStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).deleteItem(storedDocument);
        verify(auditEntryService, times(1)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    public void testDeleteDeletedStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(0)).deleteItem(storedDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

    @Test
    public void testDeleteNullStoredDocument() {
        StoredDocument storedDocument = null;
        when(storedDocumentService.findOne(TestUtil.RANDOM_UUID)).thenReturn(storedDocument);
        auditedStoredDocumentOperationsService.deleteStoredDocument(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(1)).findOne(TestUtil.RANDOM_UUID);
        verify(storedDocumentService, times(0)).deleteItem(storedDocument);
        verify(auditEntryService, times(0)).createAndSaveEntry(storedDocument, AuditActions.DELETED);
    }

}
