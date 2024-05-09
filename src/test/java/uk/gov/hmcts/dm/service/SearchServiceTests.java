package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SearchServiceTests {

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    SearchService searchService;

    @Test
    public void testSearchMetadata() {

        MetadataSearchCommand searchCommand = new MetadataSearchCommand("name", "thename");

        List<StoredDocument> documents = Arrays.asList(
                new StoredDocument(),
                new StoredDocument(),
                new StoredDocument());

        Pageable pageable = PageRequest.of(0, 2);

        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 3);

        when(this.storedDocumentRepository.findAllByMetadata(any(), any())).thenReturn(mockedPage);

        Page<StoredDocument> page = searchService.findStoredDocumentsByMetadata(searchCommand, pageable);

        Assert.assertEquals(mockedPage, page);

    }

    @Test(expected = NullPointerException.class)
    public void testSearchMetadataNullArg() {
        searchService.findStoredDocumentsByMetadata(null, null);
    }

    @Test
    public void testSearchCaseRef() {

        DeleteCaseDocumentsCommand searchCommand = new DeleteCaseDocumentsCommand("theCase");

        List<UUID> mockedDocuments = Arrays.asList(
                randomUUID(),
                randomUUID()
        );

        when(this.storedDocumentRepository.findAllByCaseRef(any())).thenReturn(mockedDocuments);

        List<StoredDocument> documents = searchService.findStoredDocumentsIdsByCaseRef(searchCommand);

        assertThat(mockedDocuments).hasSameSizeAs(documents);
        assertThat(mockedDocuments.get(0)).isEqualTo(documents.get(0).getId());
        assertThat(mockedDocuments.get(1)).isEqualTo(documents.get(1).getId());
    }

    @Test(expected = NullPointerException.class)
    public void testSearchCaseRefNullArg() {
        searchService.findStoredDocumentsIdsByCaseRef(null);
    }

    @Test
    public void testSearchByCreator() {

        List<StoredDocument> documents = Arrays.asList(
            new StoredDocument(),
            new StoredDocument(),
            new StoredDocument());

        Pageable pageable = PageRequest.of(0, 2);

        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 3);

        when(this.storedDocumentRepository.findByCreatedBy("creatorX", pageable)).thenReturn(mockedPage);

        Page<StoredDocument> page = searchService.findStoredDocumentsByCreator("creatorX", pageable);

        Assert.assertEquals(mockedPage, page);
    }

    @Test(expected = NullPointerException.class)
    public void testFindStoredDocumentsByMetadataNullPageable() {
        searchService.findStoredDocumentsByCreator("creatorX", null);
    }

    @Test(expected = NullPointerException.class)
    public void findStoredDocumentsByCreator() {
        searchService.findStoredDocumentsByCreator("x", null);
    }

}
