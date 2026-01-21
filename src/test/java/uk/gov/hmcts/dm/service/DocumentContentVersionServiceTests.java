package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentContentVersionServiceTests {

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    private DocumentContentVersionService documentContentVersionService;

    @Test
    void testFindOne() {
        UUID id = UUID.randomUUID();
        DocumentContentVersion dcv = new DocumentContentVersion();

        when(documentContentVersionRepository.findById(id)).thenReturn(Optional.of(dcv));

        Optional<DocumentContentVersion> result = documentContentVersionService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(dcv, result.get());
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileId() {
        UUID id = UUID.randomUUID();

        StoredDocument storedDocument = new StoredDocument();
        DocumentContentVersion dcv = new DocumentContentVersion();
        storedDocument.setDocumentContentVersions(Collections.singletonList(dcv));

        when(storedDocumentRepository.findByIdAndDeleted(id, false))
            .thenReturn(Optional.of(storedDocument));

        Optional<DocumentContentVersion> result =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id);

        assertTrue(result.isPresent());
        assertEquals(dcv, result.get());
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        UUID id = UUID.randomUUID();

        when(storedDocumentRepository.findByIdAndDeleted(id, false)).thenReturn(Optional.empty());

        Optional<DocumentContentVersion> result =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id);

        assertTrue(result.isEmpty());
    }
}
