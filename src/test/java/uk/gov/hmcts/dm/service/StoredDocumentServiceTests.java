package uk.gov.hmcts.dm.service;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.componenttests.TestUtil.DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.HARD_DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.STORED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.TEST_FILE;
import static uk.gov.hmcts.dm.security.Classifications.PRIVATE;

@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    void setUp() {
        when(securityUtilService.getUserId()).thenReturn("Corín Tellado");
    }

    @Test
    void testFindOne() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(STORED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument.get(), equalTo(STORED_DOCUMENT));
    }

    @Test
    void testFindOneThatDoesNotExist() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    void testFindOneThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatDoesNotExist() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatIsMarkedHardDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(HARD_DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    void testFindOneWithBinaryDataThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertTrue(storedDocument.isPresent());
    }

    @Test
    void testSave() {
        final StoredDocument storedDocument = STORED_DOCUMENT;
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    void testSaveItemsWithCommand() {
        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        Map<MultipartFile, String> mimeTypes = Map.of(TEST_FILE, Objects.requireNonNull(TEST_FILE.getContentType()));

        when(storedDocumentRepository.save(any(StoredDocument.class))).thenReturn(new StoredDocument());

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand, mimeTypes);

        final StoredDocument storedDocument = documents.getFirst();
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(PRIVATE, storedDocument.getClassification());
        assertNull(storedDocument.getMetadata());
        assertNotNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    void testSaveItemsWithCommandAndToggleConfiguration() {
        when(toggleConfiguration.isMetadatasearchendpoint()).thenReturn(true);

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        Map<MultipartFile, String> mimeTypes = Map.of(TEST_FILE, Objects.requireNonNull(TEST_FILE.getContentType()));

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand, mimeTypes);

        final StoredDocument storedDocument = documents.getFirst();
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(PRIVATE, storedDocument.getClassification());
        assertEquals(storedDocument.getMetadata(), Map.of("prop1", "value1"));
        assertNotNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    void testAddStoredDocumentVersionWhenAzureBlobStoreEnabled() {
        StoredDocument storedDocument = new StoredDocument();
        String detectedMimeType = TEST_FILE.getContentType();

        DocumentContentVersion documentContentVersion = storedDocumentService.addStoredDocumentVersion(
            storedDocument, TEST_FILE, detectedMimeType);

        assertThat(storedDocument.getDocumentContentVersions().size(), equalTo(1));
        assertThat(documentContentVersion, notNullValue());

        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().getFirst();
        assertThat(latestVersion.getMimeType(), equalTo(detectedMimeType));
        assertThat(latestVersion.getOriginalDocumentName(), equalTo(TEST_FILE.getOriginalFilename()));

        ArgumentCaptor<DocumentContentVersion> captor = ArgumentCaptor.forClass(DocumentContentVersion.class);
        verify(blobStorageWriteService).uploadDocumentContentVersion(storedDocument, documentContentVersion, TEST_FILE);
        verify(documentContentVersionRepository).save(captor.capture());
        assertThat(captor.getValue(), is(documentContentVersion));
    }

    @Test
    void testHardDeleteWithManyVersions() {
        StoredDocument storedDocumentWithContent = STORED_DOCUMENT;
        String detectedMimeType = TEST_FILE.getContentType();
        storedDocumentService.addStoredDocumentVersion(STORED_DOCUMENT, TEST_FILE, detectedMimeType);

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

        assertThat(storedDocument.isDeleted(), is(true));
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    void testHardDeleteAzureBlobEnabled() {
        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(List.of(DocumentContentVersion.builder()
                .build()))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(blobStorageDeleteService)
            .deleteDocumentContentVersion(storedDocumentWithContent.getMostRecentDocumentContentVersion());
    }

    @Test
    void testUpdateItems() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        storedDocument.setMetadata(Maps.newHashMap("Key", "Value"));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));

        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), Maps.newHashMap("UpdateKey", "UpdateValue"));
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, singletonList(update));

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
        storedDocument.setMetadata(Maps.newHashMap("Key", "Value"));

        Date newTtl = new Date();
        storedDocumentService.updateStoredDocument(storedDocument, newTtl, Maps.newHashMap("UpdateKey", "UpdateValue"));

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
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        storedDocument.setMetadata(Maps.newHashMap("Key1", "Value1"));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));
        when(toggleConfiguration.isOverridemetadata()).thenReturn(true);

        Map newMetadata = new HashMap();
        newMetadata.put("Key1", "UpdatedValue");
        newMetadata.put("Key2", "Value2");
        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), newMetadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, singletonList(update));

        storedDocumentService.updateItems(command);

        assertEquals("UpdatedValue", storedDocument.getMetadata().get("Key1"));
        assertEquals("Value2", storedDocument.getMetadata().get("Key2"));
    }

    @Test
    void testUpdateItemsOverrideFalse() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        storedDocument.setMetadata(Maps.newHashMap("Key1", "Value1"));

        when(storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(storedDocument));
        when(toggleConfiguration.isOverridemetadata()).thenReturn(false);

        Map newMetadata = new HashMap();
        newMetadata.put("Key1", "UpdatedValue");
        newMetadata.put("Key2", "Value2");
        DocumentUpdate update = new DocumentUpdate(storedDocument.getId(), newMetadata);
        UpdateDocumentsCommand command = new UpdateDocumentsCommand(null, singletonList(update));

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

        when(documentContentVersionRepository.findAllByStoredDocumentId(any(UUID.class)))
            .thenReturn(List.of(version1));
        when(storedDocumentRepository.findAllById(any()))
            .thenReturn(List.of(document1));

        List<UUID> documentIds = List.of(document1.getId(), document2.getId());

        storedDocumentService.deleteDocumentsDetails(documentIds);

        verify(blobStorageDeleteService, times(2))
            .deleteCaseDocumentBinary(any(DocumentContentVersion.class));
        verify(storedDocumentRepository, times(2)).deleteById(any(UUID.class));
    }

    @Test
    void shouldLogErrorWhenDocumentDeletionFails() {
        StoredDocument document = new StoredDocument();
        DocumentContentVersion version = new DocumentContentVersion();
        document.getDocumentContentVersions().add(version);

        when(documentContentVersionRepository.findAllByStoredDocumentId(any(UUID.class)))
            .thenReturn(List.of(version));

        doThrow(new RuntimeException("Simulated failure"))
            .when(blobStorageDeleteService).deleteCaseDocumentBinary(version);

        storedDocumentService.deleteDocumentsDetails(List.of(UUID.randomUUID()));

        verify(blobStorageDeleteService, times(1))
            .deleteCaseDocumentBinary(any(DocumentContentVersion.class));
        verify(storedDocumentRepository, times(0)).deleteById(any(UUID.class));
    }

    @Test
    void shouldSkipNullContentVersionsDuringDeletion() {
        StoredDocument document = new StoredDocument();
        document.getDocumentContentVersions().add(null);

        storedDocumentService.deleteDocumentsDetails(List.of(UUID.randomUUID()));

        verify(blobStorageDeleteService, times(0)).deleteCaseDocumentBinary(any());
        verify(storedDocumentRepository, times(1)).deleteById(any(UUID.class));
    }

}
