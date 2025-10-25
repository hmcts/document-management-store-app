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
import static org.mockito.ArgumentMatchers.any;
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
    void updateMimeType_shouldUpdateMimeTypeWhenDetectedSuccessfully() {
        UUID docId = UUID.randomUUID();
        String detectedMimeType = "application/pdf";

        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn(detectedMimeType);

        documentContentVersionService.updateMimeType(docId);

        verify(documentContentVersionRepository).updateMimeType(docId, detectedMimeType);
        verify(documentContentVersionRepository, never()).markMimeTypeUpdated(any());
    }

    @Test
    void updateMimeType_shouldMarkAsUpdatedWhenDetectionFails() {
        UUID docId = UUID.randomUUID();

        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn(null);

        documentContentVersionService.updateMimeType(docId);

        verify(documentContentVersionRepository).markMimeTypeUpdated(docId);
        verify(documentContentVersionRepository, never()).updateMimeType(any(), any());
    }

    @Test
    void updateMimeType_shouldDoNothingIfDocumentNotFound() {
        UUID docId = UUID.randomUUID();
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn(null);

        documentContentVersionService.updateMimeType(docId);

        verify(mimeTypeDetectionService).detectMimeType(docId);
        verify(documentContentVersionRepository, never()).updateMimeType(any(), any());
    }
}
