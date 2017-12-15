package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.sql.Blob;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StoredDocumentServiceTests {

    @Mock
    protected StoredDocumentRepository storedDocumentRepository;

    @Mock
    protected DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    protected BlobCreator blobCreator;

    @Mock
    protected FolderRepository folderRepository;

    @InjectMocks
    protected StoredDocumentService storedDocumentService;

    @Before
    public void setUp() throws Exception {
        when(blobCreator.createBlob(any(MultipartFile.class))).thenReturn(mock(Blob.class));
    }

    @Test
    public void testFindOne() throws Exception {
        when(this.storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(TestUtil.STORED_DOCUMENT);
        StoredDocument storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument, equalTo(TestUtil.STORED_DOCUMENT));
    }

    @Test
    public void testFindOneThatDoesNotExist() throws Exception {
        when(this.storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(null);
        StoredDocument storedDocument = storedDocumentService.findOne(TestUtil.RANDOM_UUID);
        assertThat(storedDocument, equalTo(null));
    }

    @Test
    public void testSave() throws Exception {
        final StoredDocument storedDocument = TestUtil.STORED_DOCUMENT;
        storedDocumentService.save(storedDocument);
        verify(storedDocumentRepository).save(storedDocument);
    }


    @Test
    public void testSaveItems() throws Exception {
        List<StoredDocument> documents = storedDocumentService.saveItems(singletonList(TestUtil.TEST_FILE));

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        Assert.assertEquals(1, documents.size());
        Assert.assertEquals(TestUtil.TEST_FILE.getContentType(), latestVersion.getMimeType());
        Assert.assertEquals(TestUtil.TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testAddStoredDocumentVersion() throws Exception {
        StoredDocument storedDocument = new StoredDocument();

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
    public void testDelete() throws Exception {
        StoredDocument storedDocument = new StoredDocument();
        storedDocumentService.deleteItem(storedDocument);

        assertThat(storedDocument.isDeleted(), is(true));
        verify(storedDocumentRepository).save(storedDocument);
    }

    @Test
    public void testDeleteWithNull() throws Exception {
        StoredDocument storedDocument = null;
        storedDocumentService.deleteItem(storedDocument);

        verify(storedDocumentRepository, never()).save(storedDocument);
    }

    @Test
    public void testSaveItemsToBucket() throws Exception {
        Folder folder = new Folder();

        storedDocumentService.saveItemsToBucket(folder, Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList()) );

        assertThat(folder.getStoredDocuments().size(), equalTo(1));

        final DocumentContentVersion latestVersionInFolder = folder.getStoredDocuments().get(0).getDocumentContentVersions().get(0);
        assertThat(latestVersionInFolder.getMimeType(), equalTo(TestUtil.TEST_FILE.getContentType()));
        assertThat(latestVersionInFolder.getOriginalDocumentName(), equalTo(TestUtil.TEST_FILE.getOriginalFilename()));
    }
}
