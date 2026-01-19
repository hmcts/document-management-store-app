package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTests {

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testFindStoredDocumentsByMetadata() {
        MetadataSearchCommand searchCommand = new MetadataSearchCommand("name", "thename");
        Pageable pageable = PageRequest.of(0, 2);

        List<StoredDocument> documents = List.of(
            new StoredDocument(),
            new StoredDocument(),
            new StoredDocument()
        );
        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 3);

        when(storedDocumentRepository.findAllByMetadata(searchCommand, pageable)).thenReturn(mockedPage);

        Page<StoredDocument> result = searchService.findStoredDocumentsByMetadata(searchCommand, pageable);

        assertEquals(mockedPage, result);
        verify(storedDocumentRepository).findAllByMetadata(searchCommand, pageable);
    }

    @Test
    void testFindStoredDocumentsByMetadataThrowsWithNullCommand() {
        Pageable pageable = Pageable.unpaged();
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByMetadata(null, pageable)
        );
    }

    @Test
    void testFindStoredDocumentsByMetadataThrowsWithNullPageable() {
        MetadataSearchCommand searchCommand = new MetadataSearchCommand("name", "value");
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByMetadata(searchCommand, null)
        );
    }

    @Test
    void testFindStoredDocumentsIdsByCaseRef() {
        DeleteCaseDocumentsCommand searchCommand = new DeleteCaseDocumentsCommand("theCase");
        UUID uuid1 = randomUUID();
        UUID uuid2 = randomUUID();
        List<UUID> mockedIds = List.of(uuid1, uuid2);

        when(storedDocumentRepository.findAllByCaseRef("theCase")).thenReturn(mockedIds);

        List<StoredDocument> result = searchService.findStoredDocumentsIdsByCaseRef(searchCommand);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(uuid1, result.get(0).getId());
        assertEquals(uuid2, result.get(1).getId());
    }

    @Test
    void testFindStoredDocumentsIdsByCaseRefThrowsWithNullCommand() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsIdsByCaseRef(null)
        );
    }

    @Test
    void testFindStoredDocumentsByCreator() {
        String creator = "creatorX";
        Pageable pageable = PageRequest.of(0, 2);

        List<StoredDocument> documents = List.of(
            new StoredDocument(),
            new StoredDocument()
        );
        Page<StoredDocument> mockedPage = new PageImpl<>(documents, pageable, 2);

        when(storedDocumentRepository.findByCreatedBy(creator, pageable)).thenReturn(mockedPage);

        Page<StoredDocument> result = searchService.findStoredDocumentsByCreator(creator, pageable);

        assertEquals(mockedPage, result);
        verify(storedDocumentRepository).findByCreatedBy(creator, pageable);
    }

    @Test
    void testFindStoredDocumentsByCreatorThrowsWithNullCreator() {
        Pageable pageable = Pageable.unpaged();
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByCreator(null, pageable)
        );
    }

    @Test
    void testFindStoredDocumentsByCreatorThrowsWithNullPageable() {
        assertThrows(NullPointerException.class, () ->
            searchService.findStoredDocumentsByCreator("creatorX", null)
        );
    }
}
