package uk.gov.hmcts.dm.service;

import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
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
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.componenttests.TestUtil.DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.HARD_DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.STORED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.TEST_FILE;
import static uk.gov.hmcts.dm.security.Classifications.PRIVATE;

@RunWith(SpringRunner.class)
public class StoredDocumentServiceTests {

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

    @Before
    public void setUp() {
        when(securityUtilService.getUserId()).thenReturn("Corín Tellado");
    }

    @Test
    public void testFindOne() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(STORED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument.get(), equalTo(STORED_DOCUMENT));
    }

    @Test
    public void testFindOneThatDoesNotExist() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatDoesNotExist() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatIsMarkedHardDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(HARD_DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findById(any(UUID.class))).thenReturn(Optional.of(DELETED_DOCUMENT));
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertTrue(storedDocument.isPresent());
    }

    @Test
    public void testSave() {
        final StoredDocument storedDocument = STORED_DOCUMENT;
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    public void testSaveItemsWithCommand() {
        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        when(storedDocumentRepository.save(any(StoredDocument.class))).thenReturn(new StoredDocument());
        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(PRIVATE, storedDocument.getClassification());
        Assert.assertNull(storedDocument.getMetadata());
        Assert.assertNotNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testSaveItemsWithCommandAndToggleConfiguration() {

        when(toggleConfiguration.isMetadatasearchendpoint()).thenReturn(true);

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(List.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(Map.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(PRIVATE, storedDocument.getClassification());
        assertEquals(storedDocument.getMetadata(), Map.of("prop1", "value1"));
        Assert.assertNotNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testSaveItemsToAzure() {
        List<StoredDocument> documents = storedDocumentService.saveItems(singletonList(TEST_FILE));

        assertEquals(1, documents.size());

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
        verify(blobStorageWriteService).uploadDocumentContentVersion(documents.get(0), latestVersion, TEST_FILE);
    }

    @Test
    public void testAddStoredDocumentVersionWhenAzureBlobStoreEnabled() {

        StoredDocument storedDocument = new StoredDocument();

        DocumentContentVersion documentContentVersion = storedDocumentService.addStoredDocumentVersion(
            storedDocument, TEST_FILE);

        assertThat(storedDocument.getDocumentContentVersions().size(), equalTo(1));
        assertThat(documentContentVersion, notNullValue());

        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);
        assertThat(latestVersion.getMimeType(), equalTo(TEST_FILE.getContentType()));
        assertThat(latestVersion.getOriginalDocumentName(), equalTo(TEST_FILE.getOriginalFilename()));

        ArgumentCaptor<DocumentContentVersion> captor = ArgumentCaptor.forClass(DocumentContentVersion.class);
        verify(blobStorageWriteService).uploadDocumentContentVersion(storedDocument, documentContentVersion, TEST_FILE);
        verify(documentContentVersionRepository).save(captor.capture());
        assertThat(captor.getValue(), is(documentContentVersion));
    }

    @Test
    public void testDelete() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocumentService.deleteDocument(storedDocument, false);

        assertThat(storedDocument.isDeleted(), is(true));
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    public void testHardDeleteAzureBlobEnabled() {
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
    public void testHardDeleteWithManyVersions() {

        StoredDocument storedDocumentWithContent = STORED_DOCUMENT;
        storedDocumentService.addStoredDocumentVersion(STORED_DOCUMENT, TEST_FILE);

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        storedDocumentWithContent.getDocumentContentVersions().forEach(documentContentVersion -> {
            verify(blobStorageDeleteService).deleteDocumentContentVersion(documentContentVersion);
        });
        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
    }

    @Test
    public void testUpdateItems() {
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
    public void testUpdateDocument() {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        Date newTtl = new Date();
        command.setTtl(newTtl);
        storedDocumentService.updateStoredDocument(storedDocument, command);
        assertEquals(newTtl, storedDocument.getTtl());
    }

    @Test
    public void testUpdateDocumentWithMetaData() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setMetadata(Maps.newHashMap("Key", "Value"));

        Date newTtl = new Date();
        storedDocumentService.updateStoredDocument(storedDocument, newTtl, Maps.newHashMap("UpdateKey", "UpdateValue"));

        assertEquals(newTtl, storedDocument.getTtl());
        assertEquals("Value", storedDocument.getMetadata().get("Key"));
        assertEquals("UpdateValue", storedDocument.getMetadata().get("UpdateKey"));
    }

    @Test
    public void testUpdateDeletedDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        Date newTtl = new Date();
        command.setTtl(newTtl);
        storedDocumentService.updateStoredDocument(storedDocument, command);
        Assert.assertNull(storedDocument.getTtl());
    }

    @Test
    public void testFindAllExpiredStoredDocuments() {
        storedDocumentService.findAllExpiredStoredDocuments();
        verify(storedDocumentRepository, times(1)).findByTtlLessThanAndHardDeleted(any(), any());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateStoredDocumentNullStoredDocument() {
        storedDocumentService.updateStoredDocument(null, new UpdateDocumentCommand());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateStoredDocumentNullCommand() {
        storedDocumentService.updateStoredDocument(new StoredDocument(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateMigratedStoredDocumentNullStoredDocument() {
        storedDocumentService.updateMigratedStoredDocument(null, null);
    }

    @Test
    public void testUpdateMigratedStoredDocumentIsDeleted() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        storedDocumentService.updateMigratedStoredDocument(storedDocument, null);
        verify(storedDocumentRepository, times(0)).save(any());
    }

    @Test
    public void testUpdateMigratedStoredDocumentNullMetadata() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(false);
        storedDocumentService.updateMigratedStoredDocument(storedDocument, null);
        verify(storedDocumentRepository, times(0)).save(any());
    }

    @Test
    public void testUpdateItemsOverrideTrue() {
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
    public void testUpdateItemsOverrideFalse() {
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
}
