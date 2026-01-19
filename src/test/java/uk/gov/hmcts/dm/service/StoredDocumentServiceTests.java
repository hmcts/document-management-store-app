package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.security.Classifications.PRIVATE;

@ExtendWith(MockitoExtension.class)
class StoredDocumentServiceTests {

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    private ToggleConfiguration toggleConfiguration;

    @Mock
    private BlobStorageWriteService blobStorageWriteService;

    @Mock
    private SecurityUtilService securityUtilService;

    @Mock
    private BlobStorageDeleteService blobStorageDeleteService;

    @InjectMocks
    private StoredDocumentService storedDocumentService;

    private final MockMultipartFile testFile = new MockMultipartFile(
        "file",
        "filename.txt",
        "text/plain",
        "some content".getBytes(StandardCharsets.UTF_8)
    );

    @BeforeEach
    void setUp() {
        lenient().when(securityUtilService.getUserId()).thenReturn("Corín Tellado");
    }

    @Test
    void testFindOne() {
        StoredDocument storedDocument = new StoredDocument();
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));

        Optional<StoredDocument> result = storedDocumentService.findOne(UUID.randomUUID());

        assertTrue(result.isPresent());
        assertEquals(storedDocument, result.get());
    }

    @Test
    void testFindOneThatDoesNotExist() {
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<StoredDocument> result = storedDocumentService.findOne(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindOneThatIsMarkedDeleted() {
        StoredDocument deletedDoc = new StoredDocument();
        deletedDoc.setDeleted(true);
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(deletedDoc));

        Optional<StoredDocument> result = storedDocumentService.findOne(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatDoesNotExist() {
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<StoredDocument> result = storedDocumentService.findOneWithBinaryData(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatIsMarkedHardDeleted() {
        StoredDocument hardDeletedDoc = new StoredDocument();
        hardDeletedDoc.setHardDeleted(true);
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(hardDeletedDoc));

        Optional<StoredDocument> result = storedDocumentService.findOneWithBinaryData(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatIsMarkedDeleted() {
        StoredDocument deletedDoc = new StoredDocument();
        deletedDoc.setDeleted(true);
        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(deletedDoc));

        Optional<StoredDocument> result = storedDocumentService.findOneWithBinaryData(UUID.randomUUID());

        assertTrue(result.isPresent());
    }

    @Test
    void testSave() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    void testSaveItemsWithCommand() {
        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(List.of(testFile));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        Map<MultipartFile, String> mimeTypes = Map.of(testFile, Objects.requireNonNull(testFile.getContentType()));

        when(storedDocumentRepository.save(any(StoredDocument.class))).thenReturn(new StoredDocument());

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand, mimeTypes);

        assertEquals(1, documents.size());
        final StoredDocument storedDocument = documents.getFirst();
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();

        assertEquals(Set.of("a", "b"), storedDocument.getRoles());
        assertEquals(PRIVATE, storedDocument.getClassification());
        assertNull(storedDocument.getMetadata()); // Null because toggleConfiguration.isMetadatasearchendpoint() is false by default mock
        assertNotNull(storedDocument.getTtl());
        assertEquals(testFile.getContentType(), latestVersion.getMimeType());
        assertEquals(testFile.getOriginalFilename(), latestVersion.getOriginalDocumentName());
        assertTrue(latestVersion.isMimeTypeUpdated());
    }

    @Test
    void testSaveItemsWithCommandAndToggleConfiguration() {
        when(toggleConfiguration.isMetadatasearchendpoint()).thenReturn(true);

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(List.of(testFile));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        Map<MultipartFile, String> mimeTypes = Map.of(testFile, Objects.requireNonNull(testFile.getContentType()));

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand, mimeTypes);

        assertEquals(1, documents.size());
        final StoredDocument storedDocument = documents.getFirst();
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();

        assertEquals(Set.of("a", "b"), storedDocument.getRoles());
        assertEquals(PRIVATE, storedDocument.getClassification());
        assertEquals(Map.of("prop1", "value1"), storedDocument.getMetadata());
        assertNotNull(storedDocument.getTtl());
        assertEquals(testFile.getContentType(), latestVersion.getMimeType());
        assertEquals(testFile.getOriginalFilename(), latestVersion.getOriginalDocumentName());
        assertTrue(latestVersion.isMimeTypeUpdated());
    }

    @Test
    void testAddStoredDocumentVersionWhenAzureBlobStoreEnabled() {
        StoredDocument storedDocument = new StoredDocument();
        String detectedMimeType = testFile.getContentType();

        DocumentContentVersion documentContentVersion = storedDocumentService.addStoredDocumentVersion(
            storedDocument, testFile, detectedMimeType);

        assertEquals(1, storedDocument.getDocumentContentVersions().size());
        assertNotNull(documentContentVersion);

        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();
        assertEquals(detectedMimeType, latestVersion.getMimeType());
        assertEquals(testFile.getOriginalFilename(), latestVersion.getOriginalDocumentName());

        ArgumentCaptor<DocumentContentVersion> captor = ArgumentCaptor.forClass(DocumentContentVersion.class);
        verify(blobStorageWriteService).uploadDocumentContentVersion(storedDocument, documentContentVersion, testFile);
        verify(documentContentVersionRepository).save(captor.capture());
        assertEquals(documentContentVersion, captor.getValue());
    }

    @Test
    void testHardDeleteWithManyVersions() {
        StoredDocument storedDocumentWithContent = new StoredDocument();
        storedDocumentService.addStoredDocumentVersion(storedDocumentWithContent, testFile, testFile.getContentType());

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        storedDocumentWithContent.getDocumentContentVersions().forEach(documentContentVersion -> {
            verify(blobStorageDeleteService).deleteDocumentContentVersion(documentContentVersion);
        });
        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
    }

    @Test
    void testDelete() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocumentService.deleteDocument(storedDocument, false);

        assertTrue(storedDocument.isDeleted());
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    void testHardDeleteAzureBlobEnabled() {
        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(List.of(DocumentContentVersion.builder().build()))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(blobStorageDeleteService)
            .deleteDocumentContentVersion(storedDocumentWithContent.getMostRecentDocumentContentVersion());
    }

    @Test
    void testUpdateItems() {
        UUID docId = UUID.randomUUID();
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(docId);
        storedDocument.setMetadata(new HashMap<>(Map.of("Key", "Value")));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));

        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), new HashMap<>(Map.of("UpdateKey", "UpdateValue")));
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, List.of(update));

        storedDocumentService.updateItems(command);

        assertEquals("Value", storedDocument.getMetadata().get("Key"));
        assertEquals("UpdateValue", storedDocument.getMetadata().get("UpdateKey"));
    }

    @Test
    void testUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        Date newTtl = new Date();
        command.setTtl(newTtl);
        storedDocumentService.updateStoredDocument(storedDocument, command);
        assertEquals(newTtl, storedDocument.getTtl());
    }

    @Test
    void testUpdateDocumentWithMetaData() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setMetadata(new HashMap<>(Map.of("Key", "Value")));

        Date newTtl = new Date();
        storedDocumentService.updateStoredDocument(storedDocument, newTtl, Map.of("UpdateKey", "UpdateValue"));

        assertEquals(newTtl, storedDocument.getTtl());
        assertEquals("Value", storedDocument.getMetadata().get("Key"));
        assertEquals("UpdateValue", storedDocument.getMetadata().get("UpdateKey"));
    }

    @Test
    void testUpdateDeletedDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        Date newTtl = new Date();
        command.setTtl(newTtl);
        storedDocumentService.updateStoredDocument(storedDocument, command);
        assertNull(storedDocument.getTtl());
    }

    @Test
    void testFindAllExpiredStoredDocuments() {
        storedDocumentService.findAllExpiredStoredDocuments();
        verify(storedDocumentRepository, times(1)).findByTtlLessThanAndHardDeleted(any(), any());
    }

    @Test
    void testUpdateStoredDocumentNullStoredDocument() {
        var updateCommand = new UpdateDocumentCommand();
        assertThrows(NullPointerException.class, () ->
            storedDocumentService.updateStoredDocument(null, updateCommand)
        );
    }

    @Test
    void testUpdateStoredDocumentNullCommand() {
        var storedDoc = new StoredDocument();
        assertThrows(NullPointerException.class, () ->
            storedDocumentService.updateStoredDocument(storedDoc, null)
        );
    }

    @Test
    void testUpdateMigratedStoredDocumentNullStoredDocument() {
        assertThrows(NullPointerException.class, () ->
            storedDocumentService.updateMigratedStoredDocument(null, null)
        );
    }

    @Test
    void testUpdateMigratedStoredDocumentIsDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        storedDocumentService.updateMigratedStoredDocument(storedDocument, null);
        verify(storedDocumentRepository, times(0)).save(any());
    }

    @Test
    void testUpdateMigratedStoredDocumentNullMetadata() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(false);
        storedDocumentService.updateMigratedStoredDocument(storedDocument, null);
        verify(storedDocumentRepository, times(0)).save(any());
    }

    @Test
    void testUpdateItemsOverrideTrue() {
        UUID docId = UUID.randomUUID();
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(docId);
        storedDocument.setMetadata(new HashMap<>(Map.of("Key1", "Value1")));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));
        when(toggleConfiguration.isOverridemetadata()).thenReturn(true);

        Map<String, String> newMetadata = new HashMap<>();
        newMetadata.put("Key1", "UpdatedValue");
        newMetadata.put("Key2", "Value2");
        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), newMetadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, List.of(update));

        storedDocumentService.updateItems(command);

        assertEquals("UpdatedValue", storedDocument.getMetadata().get("Key1"));
        assertEquals("Value2", storedDocument.getMetadata().get("Key2"));
    }

    @Test
    void testUpdateItemsOverrideFalse() {
        UUID docId = UUID.randomUUID();
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(docId);
        storedDocument.setMetadata(new HashMap<>(Map.of("Key1", "Value1")));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));
        when(toggleConfiguration.isOverridemetadata()).thenReturn(false);

        Map<String, String> newMetadata = new HashMap<>();
        newMetadata.put("Key1", "UpdatedValue");
        newMetadata.put("Key2", "Value2");
        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), newMetadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, List.of(update));

        storedDocumentService.updateItems(command);

        assertEquals("Value1", storedDocument.getMetadata().get("Key1"));
        assertEquals("Value2", storedDocument.getMetadata().get("Key2"));
    }

    @Test
    void shouldDeleteAllDocumentsAndLogCompletion() {
        StoredDocument document1 = new StoredDocument();
        document1.setId(UUID.randomUUID());
        DocumentContentVersion version1 = new DocumentContentVersion();
        document1.getDocumentContentVersions().add(version1);

        StoredDocument document2 = new StoredDocument();
        document2.setId(UUID.randomUUID());
        DocumentContentVersion version2 = new DocumentContentVersion();
        document2.getDocumentContentVersions().add(version2);

        when(documentContentVersionRepository.findAllByStoredDocumentId(document1.getId()))
            .thenReturn(List.of(version1));
        when(documentContentVersionRepository.findAllByStoredDocumentId(document2.getId()))
            .thenReturn(List.of(version2));

        List<UUID> documentIds = List.of(document1.getId(), document2.getId());

        storedDocumentService.deleteDocumentsDetails(documentIds);

        verify(blobStorageDeleteService, times(2))
            .deleteCaseDocumentBinary(any(DocumentContentVersion.class));
        verify(storedDocumentRepository, times(2)).deleteById(any(UUID.class));
    }

    @Test
    void shouldLogErrorWhenDocumentDeletionFails() {
        UUID docId = UUID.randomUUID();
        StoredDocument document = new StoredDocument();
        document.setId(docId);
        DocumentContentVersion version = new DocumentContentVersion();
        document.getDocumentContentVersions().add(version);

        when(documentContentVersionRepository.findAllByStoredDocumentId(docId))
            .thenReturn(List.of(version));

        doThrow(new RuntimeException("Simulated failure"))
            .when(blobStorageDeleteService).deleteCaseDocumentBinary(version);

        storedDocumentService.deleteDocumentsDetails(List.of(docId));

        verify(blobStorageDeleteService, times(1))
            .deleteCaseDocumentBinary(any(DocumentContentVersion.class));
        verify(storedDocumentRepository, times(0)).deleteById(any(UUID.class));
    }

    @Test
    void shouldSkipNullContentVersionsDuringDeletion() {
        UUID docId = UUID.randomUUID();
        StoredDocument document = new StoredDocument();
        document.setId(docId);

        List<DocumentContentVersion> versions = new ArrayList<>();
        versions.add(null);

        when(documentContentVersionRepository.findAllByStoredDocumentId(docId))
            .thenReturn(versions);

        storedDocumentService.deleteDocumentsDetails(List.of(docId));

        verify(blobStorageDeleteService, times(0)).deleteCaseDocumentBinary(any());
        verify(storedDocumentRepository, times(1)).deleteById(docId);
    }
}
