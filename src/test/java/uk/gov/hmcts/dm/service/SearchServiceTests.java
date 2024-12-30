package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SearchServiceTests {

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    SearchService searchService;

    @Test
    void testSearchMetadata() {

        MetadataSearchCommand searchCommand = new MetadataSearchCommand("name", "thename");

        List<StoredDocument> documents = Arrays.asList(
                new StoredDocument(),
                new StoredDocument(),
                new StoredDocument());

        Pageable pageable = PageRequest.of(0, 2);

        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 3);

        when(this.storedDocumentRepository.findAllByMetadata(any(), any())).thenReturn(mockedPage);

        Page<StoredDocument> page = searchService.findStoredDocumentsByMetadata(searchCommand, pageable);

        assertEquals(mockedPage, page);

    }

    @Test
    void testSearchMetadataNullArg() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByMetadata(null, null)
        );
    }

    @Test
    void testSearchCaseRef() {

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

    @Test
    void testSearchCaseRefNullArg() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsIdsByCaseRef(null)
        );
    }
    @Test
    void testSearchByCreator() {

        List<StoredDocument> documents = Arrays.asList(
            new StoredDocument(),
            new StoredDocument(),
            new StoredDocument());

        Pageable pageable = PageRequest.of(0, 2);

        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 3);

        when(this.storedDocumentRepository.findByCreatedBy("creatorX", pageable)).thenReturn(mockedPage);

        Page<StoredDocument> page = searchService.findStoredDocumentsByCreator("creatorX", pageable);

        assertEquals(mockedPage, page);
    }

    @Test
    void testFindStoredDocumentsByMetadataNullPageable() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByCreator("creatorX", null)
        );
    }

    @Test
    void findStoredDocumentsByCreator() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByCreator("x", null)
        );
    }
}
