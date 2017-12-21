package uk.gov.hmcts.reform.dm.service;

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
import uk.gov.hmcts.reform.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.reform.dm.domain.StoredDocument;
import uk.gov.hmcts.reform.dm.repository.StoredDocumentRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Created by pawel on 11/07/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTests {

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    public void testSearchMetadata() throws Exception {

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
    public void testSearchMetadataNullArg() throws Exception {
        searchService.findStoredDocumentsByMetadata(null, null);
    }

}
