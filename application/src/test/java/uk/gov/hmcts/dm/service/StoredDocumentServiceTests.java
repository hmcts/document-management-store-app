package uk.gov.hmcts.dm.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentRepository;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.sql.Blob;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.componenttests.TestUtil.DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.HARD_DELETED_DOCUMENT;
import static uk.gov.hmcts.dm.componenttests.TestUtil.TEST_FILE;
import static uk.gov.hmcts.dm.security.Classifications.PRIVATE;

@RunWith(MockitoJUnitRunner.class)
public class StoredDocumentServiceTests {

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    private DocumentContentRepository documentContentRepository;

    @Mock
    private ToggleConfiguration toggleConfiguration;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private BlobStorageWriteService blobStorageWriteService;

    @Mock
    private SecurityUtilService securityUtilService;

    @Mock
    private AzureStorageConfiguration azureStorageConfiguration;

    @InjectMocks
    private StoredDocumentService storedDocumentService;

    @Before
    public void setUp() {
        when(securityUtilService.getUserId()).thenReturn("Corín Tellado");
    }

    @Test
    public void testFindOne() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(TestUtil.STORED_DOCUMENT);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument.get(), equalTo(TestUtil.STORED_DOCUMENT));
    }

    @Test
    public void testFindOneThatDoesNotExist() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(null);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(DELETED_DOCUMENT);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatDoesNotExist() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(null);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatIsMarkedHardDeleted() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(HARD_DELETED_DOCUMENT);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertFalse(storedDocument.isPresent());
    }

    @Test
    public void testFindOneWithBinaryDataThatIsMarkedDeleted() {
        when(this.storedDocumentRepository.findOne(any(UUID.class))).thenReturn(DELETED_DOCUMENT);
        Optional<StoredDocument> storedDocument = storedDocumentService.findOneWithBinaryData(TestUtil.RANDOM_UUID);
        assertTrue(storedDocument.isPresent());
    }

    @Test
    public void testSave() {
        final StoredDocument storedDocument = TestUtil.STORED_DOCUMENT;
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    public void testSaveItemsWithCommand() {
        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(ImmutableList.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(ImmutableMap.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        when(storedDocumentRepository.save(any(StoredDocument.class))).thenReturn(new StoredDocument());
        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(storedDocument.getClassification(), PRIVATE);
        Assert.assertNull(storedDocument.getMetadata());
        Assert.assertNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testSaveItemsWithCommandAndToggleConfiguration() {

        when(toggleConfiguration.isMetadatasearchendpoint()).thenReturn(true);
        when(toggleConfiguration.isTtl()).thenReturn(true);

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TEST_FILE));
        uploadDocumentsCommand.setRoles(ImmutableList.of("a", "b"));
        uploadDocumentsCommand.setClassification(PRIVATE);
        uploadDocumentsCommand.setMetadata(ImmutableMap.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        assertEquals(1, documents.size());
        assertEquals(storedDocument.getRoles(), newHashSet("a", "b"));
        assertEquals(storedDocument.getClassification(), PRIVATE);
        assertEquals(storedDocument.getMetadata(), ImmutableMap.of("prop1", "value1"));
        Assert.assertNotNull(storedDocument.getTtl());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testSaveItems() {
        List<StoredDocument> documents = storedDocumentService.saveItems(singletonList(TEST_FILE));

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        assertEquals(1, documents.size());
        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
        verifyNoMoreInteractions(blobStorageWriteService);
    }

    @Test
    public void testSaveItemsToAzure() {
        setupStorageOptions(true, false);
        List<StoredDocument> documents = storedDocumentService.saveItems(singletonList(TEST_FILE));

        assertEquals(1, documents.size());

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        assertEquals(TEST_FILE.getContentType(), latestVersion.getMimeType());
        assertEquals(TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
        verify(blobStorageWriteService).uploadDocumentContentVersion(documents.get(0), latestVersion, TEST_FILE);
    }

    @Test
    public void testAddStoredDocumentVersion() {

        setupStorageOptions(false, true);
        StoredDocument storedDocument = new StoredDocument();

        DocumentContentVersion documentContentVersion = storedDocumentService.addStoredDocumentVersion(
            storedDocument, TEST_FILE);

        assertThat(storedDocument.getDocumentContentVersions().size(), equalTo(1));

        assertThat(documentContentVersion, notNullValue());

        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);
        assertThat(latestVersion.getMimeType(), equalTo(TEST_FILE.getContentType()));
        assertThat(latestVersion.getOriginalDocumentName(), equalTo(TEST_FILE.getOriginalFilename()));

        ArgumentCaptor<DocumentContentVersion> captor = ArgumentCaptor.forClass(DocumentContentVersion.class);
        verify(documentContentVersionRepository).save(captor.capture());
        assertThat(captor.getValue(), is(documentContentVersion));
    }

    @Test
    public void testAddStoredDocumentVersionWhenAzureBlobStoreEnabled() {

        setupStorageOptions(true, false);
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
    public void testHardDelete() {
        DocumentContent documentContent = new DocumentContent(mock(Blob.class));
        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(ImmutableList.of(DocumentContentVersion.builder()
                .documentContent(documentContent)
                .build()))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        assertThat(storedDocumentWithContent.getMostRecentDocumentContentVersion().getDocumentContent(), nullValue());
        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(documentContentRepository).delete(documentContent);
    }

    @Test
    public void testHardDeleteWithManyVersions() {
        DocumentContentVersion contentVersion = DocumentContentVersion.builder()
            .documentContent(new DocumentContent(mock(Blob.class)))
            .build();

        DocumentContentVersion secondContentVersion = DocumentContentVersion.builder()
            .documentContent(new DocumentContent(mock(Blob.class)))
            .build();

        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(Arrays.asList(contentVersion, secondContentVersion))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        storedDocumentWithContent.getDocumentContentVersions().forEach(documentContentVersion -> {
            assertThat(documentContentVersion.getDocumentContent(), nullValue());
        });
        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(documentContentRepository, times(2)).delete(Mockito.any(DocumentContent.class));
    }

    @Test
    public void testSaveItemsToBucket() {
        Folder folder = new Folder();

        storedDocumentService.saveItemsToBucket(folder, Stream.of(TEST_FILE).collect(Collectors.toList()));

        assertThat(folder.getStoredDocuments().size(), equalTo(1));

        final DocumentContentVersion latestVersionInFolder = folder.getStoredDocuments().get(0).getDocumentContentVersions().get(0);

        assertThat(latestVersionInFolder.getMimeType(), equalTo(TEST_FILE.getContentType()));
        assertThat(latestVersionInFolder.getOriginalDocumentName(), equalTo(TEST_FILE.getOriginalFilename()));
        verify(securityUtilService).getUserId();
        verify(folderRepository).save(folder);
        verifyNoMoreInteractions(blobStorageWriteService);
    }

    @Test
    public void testSaveItemsToBucketToBlobStore() throws Exception {
        Folder folder = new Folder();
        setupStorageOptions(true, false);
        storedDocumentService.saveItemsToBucket(folder, Stream.of(TEST_FILE).collect(Collectors.toList()));

        assertThat(folder.getStoredDocuments().size(), equalTo(1));

        final DocumentContentVersion latestVersionInFolder = folder.getStoredDocuments().get(0).getDocumentContentVersions().get(0);

        assertThat(latestVersionInFolder.getMimeType(), equalTo(TEST_FILE.getContentType()));
        assertThat(latestVersionInFolder.getOriginalDocumentName(), equalTo(TEST_FILE.getOriginalFilename()));
        verify(securityUtilService).getUserId();
        verify(folderRepository).save(folder);
        verify(blobStorageWriteService).uploadDocumentContentVersion(folder.getStoredDocuments().get(0), latestVersionInFolder, TEST_FILE);
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

    private void setupStorageOptions(Boolean azureEnabled, Boolean postgresEnabled) {
        when(azureStorageConfiguration.isAzureBlobStoreEnabled()).thenReturn(azureEnabled);
        when(azureStorageConfiguration.isPostgresBlobStorageEnabled()).thenReturn(postgresEnabled);
    }
}
