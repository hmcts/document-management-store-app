package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

        Pageable pageable = new PageRequest(0, 2);

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
    public void testSearchByCreator() {

        List<StoredDocument> documents = Arrays.asList(
            new StoredDocument(),
            new StoredDocument(),
            new StoredDocument());

        Pageable pageable = new PageRequest(0, 2);

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
