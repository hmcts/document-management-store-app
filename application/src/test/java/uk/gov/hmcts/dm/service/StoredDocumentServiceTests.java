package uk.gov.hmcts.dm.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by pawel on 11/07/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class StoredDocumentServiceTests {

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @Mock
    DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    FolderRepository folderRepository;

    @Mock
    ToggleConfiguration toggleConfiguration;

    @Mock
    SecurityUtilService securityUtilService;

    @Mock
    FileStorageService fileStorageService;

    @InjectMocks
    StoredDocumentService storedDocumentService;

    @Before
    public void setUp() {
    }

    @Test
    public void testFindOne() {
        when(this.storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(TestUtil.STORED_DOCUMENT);
        StoredDocument storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument, equalTo(TestUtil.STORED_DOCUMENT));
    }

    @Test
    public void testFindOneThatDoesNotExist() {
        when(this.storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(null);
        StoredDocument storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument, equalTo(null));
    }

    @Test
    public void testSave() {
        final StoredDocument storedDocument = TestUtil.STORED_DOCUMENT;
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }


    @Test
    public void testSaveItemsWithCommand() throws Exception {
        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TestUtil.TEST_FILE));
        uploadDocumentsCommand.setRoles(ImmutableList.of("a", "b"));
        uploadDocumentsCommand.setClassification(Classifications.PRIVATE);
        uploadDocumentsCommand.setMetadata(ImmutableMap.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        DocumentContentVersion documentContentVersion =
            new DocumentContentVersion(UUID.randomUUID(), new StoredDocument(), TestUtil.TEST_FILE, "x");

        when(fileStorageService
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull()))
            .thenReturn(documentContentVersion);

        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        verify(fileStorageService, times(1))
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull());

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        Assert.assertEquals(1, documents.size());
        Assert.assertEquals(storedDocument.getRoles(), new HashSet(ImmutableList.of("a", "b")));
        Assert.assertEquals(storedDocument.getClassification(), Classifications.PRIVATE);
        Assert.assertNull(storedDocument.getMetadata());
        Assert.assertNull(storedDocument.getTtl());
        Assert.assertEquals(TestUtil.TEST_FILE.getContentType(), latestVersion.getMimeType());
        Assert.assertEquals(TestUtil.TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }


    @Test
    public void testSaveItemsWithCommandAndToggleConfiguration() throws Exception {

        when(toggleConfiguration.isMetadatasearchendpoint()).thenReturn(true);
        when(toggleConfiguration.isTtl()).thenReturn(true);

        UploadDocumentsCommand uploadDocumentsCommand = new UploadDocumentsCommand();
        uploadDocumentsCommand.setFiles(singletonList(TestUtil.TEST_FILE));
        uploadDocumentsCommand.setRoles(ImmutableList.of("a", "b"));
        uploadDocumentsCommand.setClassification(Classifications.PRIVATE);
        uploadDocumentsCommand.setMetadata(ImmutableMap.of("prop1", "value1"));
        uploadDocumentsCommand.setTtl(new Date());

        DocumentContentVersion documentContentVersion =
            new DocumentContentVersion(UUID.randomUUID(), new StoredDocument(), TestUtil.TEST_FILE, "x");

        when(fileStorageService
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull()))
            .thenReturn(documentContentVersion);


        List<StoredDocument> documents = storedDocumentService.saveItems(uploadDocumentsCommand);

        final StoredDocument storedDocument = documents.get(0);
        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);

        Assert.assertEquals(1, documents.size());
        Assert.assertEquals(storedDocument.getRoles(), new HashSet(ImmutableList.of("a", "b")));
        Assert.assertEquals(storedDocument.getClassification(), Classifications.PRIVATE);
        Assert.assertEquals(storedDocument.getMetadata(), ImmutableMap.of("prop1", "value1"));
        Assert.assertNotNull(storedDocument.getTtl());
        Assert.assertEquals(TestUtil.TEST_FILE.getContentType(), latestVersion.getMimeType());
        Assert.assertEquals(TestUtil.TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testSaveItems() throws Exception {

        DocumentContentVersion documentContentVersion =
            new DocumentContentVersion(UUID.randomUUID(), new StoredDocument(), TestUtil.TEST_FILE, "x");

        when(fileStorageService
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull()))
            .thenReturn(documentContentVersion);

        List<StoredDocument> documents = storedDocumentService.saveItems(singletonList(TestUtil.TEST_FILE));

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        Assert.assertEquals(1, documents.size());
        Assert.assertEquals(TestUtil.TEST_FILE.getContentType(), latestVersion.getMimeType());
        Assert.assertEquals(TestUtil.TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }



    @Test
    public void testAddStoredDocumentVersion() {
        StoredDocument storedDocument = new StoredDocument();

        DocumentContentVersion mockedDocumentContentVersion =
            new DocumentContentVersion(UUID.randomUUID(), new StoredDocument(), TestUtil.TEST_FILE, "x");

        when(fileStorageService
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull()))
            .thenReturn(mockedDocumentContentVersion);

        DocumentContentVersion documentContentVersion = storedDocumentService.addStoredDocumentVersion(
            storedDocument, TestUtil.TEST_FILE
        );

        assertThat(storedDocument.getDocumentContentVersions().size(), equalTo(1));

        assertThat(documentContentVersion, notNullValue());

        final DocumentContentVersion latestVersion = storedDocument.getDocumentContentVersions().get(0);
        assertThat(latestVersion.getMimeType(), equalTo(TestUtil.TEST_FILE.getContentType()));
        assertThat(latestVersion.getOriginalDocumentName(), equalTo(TestUtil.TEST_FILE.getOriginalFilename()));
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
        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(ImmutableList.of(DocumentContentVersion.builder()
                .build()))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(fileStorageService, atLeastOnce()).delete(storedDocumentWithContent.getMostRecentDocumentContentVersion());
    }

    @Test
    public void testHardDeleteWithManyVersions() {
        DocumentContentVersion contentVersion = DocumentContentVersion.builder()
            .build();

        DocumentContentVersion secondContentVersion = DocumentContentVersion.builder()
            .build();

        StoredDocument storedDocumentWithContent = StoredDocument.builder()
            .documentContentVersions(Arrays.asList(contentVersion, secondContentVersion))
            .build();

        storedDocumentService.deleteDocument(storedDocumentWithContent, true);

        verify(storedDocumentRepository, atLeastOnce()).save(storedDocumentWithContent);
        verify(fileStorageService, times(2)).delete(Mockito.any(DocumentContentVersion.class));
    }

    @Test
    public void testSaveItemsToBucket() throws Exception {
        Folder folder = new Folder();

        DocumentContentVersion documentContentVersion =
            new DocumentContentVersion(UUID.randomUUID(), new StoredDocument(), TestUtil.TEST_FILE, "x");

        when(fileStorageService
            .uploadFile(any(StoredDocument.class), any(MultipartFile.class), isNull()))
            .thenReturn(documentContentVersion);

        storedDocumentService.saveItemsToBucket(folder, Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList()));

        assertThat(folder.getStoredDocuments().size(), equalTo(1));

        final DocumentContentVersion latestVersionInFolder = folder.getStoredDocuments().get(0).getDocumentContentVersions().get(0);
        assertThat(latestVersionInFolder.getMimeType(), equalTo(TestUtil.TEST_FILE.getContentType()));
        assertThat(latestVersionInFolder.getOriginalDocumentName(), equalTo(TestUtil.TEST_FILE.getOriginalFilename()));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        StoredDocument storedDocument = new StoredDocument();
        UpdateDocumentCommand command = new UpdateDocumentCommand();
        Date newTtl = new Date();
        command.setTtl(newTtl);
        storedDocumentService.updateStoredDocument(storedDocument, command);
        Assert.assertEquals(newTtl, storedDocument.getTtl());
    }

    @Test
    public void testUpdateDeletedDocument() throws Exception {
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
}
