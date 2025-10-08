package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentContentVersionServiceTests {

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @Mock
    private MimeTypeDetectionService mimeTypeDetectionService;

    @InjectMocks
    private DocumentContentVersionService documentContentVersionService;

    @Test
    void testFindOne() {
        when(documentContentVersionRepository
            .findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(new DocumentContentVersion()));
        assertNotNull(documentContentVersionService.findById(TestUtil.RANDOM_UUID));
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository
            .findByIdAndDeleted(TestUtil.RANDOM_UUID, false))
                .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));
        assertEquals(Optional.of(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion()),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findByIdAndDeleted(TestUtil.RANDOM_UUID, false)).thenReturn(Optional.empty());
        assertEquals(Optional.empty(),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    void updateMimeType_shouldUpdateMimeTypeWhenDetectedIsDifferent() {
        // Given
        UUID docId = UUID.randomUUID();
        DocumentContentVersion version = new DocumentContentVersion();
        version.setMimeType("application/octet-stream");
        version.setMimeTypeUpdated(false);

        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.of(version));
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn("application/pdf");

        // When
        documentContentVersionService.updateMimeType(docId);

        // Then
        assertEquals("application/pdf", version.getMimeType());
        assertTrue(version.isMimeTypeUpdated());
    }

    @Test
    void updateMimeType_shouldNotUpdateMimeTypeWhenDetectedIsTheSame() {
        // Given
        UUID docId = UUID.randomUUID();
        DocumentContentVersion version = new DocumentContentVersion();
        version.setMimeType("application/pdf");
        version.setMimeTypeUpdated(false);

        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.of(version));
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn("application/pdf");

        // When
        documentContentVersionService.updateMimeType(docId);

        // Then
        verify(documentContentVersionRepository, never()).save(version); // Assuming transactional boundary handles save
        assertEquals("application/pdf", version.getMimeType());
        assertTrue(version.isMimeTypeUpdated());
    }

    @Test
    void updateMimeType_shouldMarkAsUpdatedWhenDetectionFails() {
        // Given
        UUID docId = UUID.randomUUID();
        DocumentContentVersion version = new DocumentContentVersion();
        version.setMimeType("application/octet-stream");
        version.setMimeTypeUpdated(false);

        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.of(version));
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn(null);

        // When
        documentContentVersionService.updateMimeType(docId);

        // Then
        assertEquals("application/octet-stream", version.getMimeType()); // MimeType should not change
        assertTrue(version.isMimeTypeUpdated()); // Should be marked as updated to avoid reprocessing
    }

    @Test
    void updateMimeType_shouldDoNothingIfDocumentNotFound() {
        // Given
        UUID docId = UUID.randomUUID();
        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.empty());
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn("application/pdf");

        // When
        documentContentVersionService.updateMimeType(docId);

        // Then
        // No exception should be thrown, and the method should complete gracefully.
        // We can also verify that the mimeTypeDetectionService was still called.
        verify(mimeTypeDetectionService).detectMimeType(docId);
    }
}
