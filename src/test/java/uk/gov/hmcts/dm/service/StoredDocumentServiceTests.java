package uk.gov.hmcts.dm.service;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.TestUtil;
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
import java.util.List;
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
    DocumentContentRepository documentContentRepository;

    @Mock
    BlobCreator blobCreator;

    @Mock
    FolderRepository folderRepository;

    @InjectMocks
    StoredDocumentService storedDocumentService;

    @Before
    public void setUp() {
        when(blobCreator.createBlob(any(MultipartFile.class))).thenReturn(mock(Blob.class));
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
    public void testSaveItems() throws Exception {
        List<StoredDocument> documents = storedDocumentService.saveDocuments(singletonList(TestUtil.TEST_FILE));

        final DocumentContentVersion latestVersion = documents.get(0).getDocumentContentVersions().get(0);

        Assert.assertEquals(1, documents.size());
        Assert.assertEquals(TestUtil.TEST_FILE.getContentType(), latestVersion.getMimeType());
        Assert.assertEquals(TestUtil.TEST_FILE.getOriginalFilename(), latestVersion.getOriginalDocumentName());
    }

    @Test
    public void testAddStoredDocumentVersion() {
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
    public void testSaveItemsToBucket() throws Exception {
        Folder folder = new Folder();

        storedDocumentService.saveItemsToBucket(folder, Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList()));

        assertThat(folder.getStoredDocuments().size(), equalTo(1));

        final DocumentContentVersion latestVersionInFolder = folder.getStoredDocuments().get(0).getDocumentContentVersions().get(0);
        assertThat(latestVersionInFolder.getMimeType(), equalTo(TestUtil.TEST_FILE.getContentType()));
        assertThat(latestVersionInFolder.getOriginalDocumentName(), equalTo(TestUtil.TEST_FILE.getOriginalFilename()));
    }
}
