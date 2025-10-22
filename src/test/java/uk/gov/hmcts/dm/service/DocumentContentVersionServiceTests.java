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
    void updateMimeType_shouldUpdateMimeTypeWhenDetectedIsDifferent() {
        UUID docId = UUID.randomUUID();
        DocumentContentVersion version = new DocumentContentVersion();
        version.setMimeType("application/octet-stream");

        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.of(version));
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn("application/pdf");

        documentContentVersionService.updateMimeType(docId);

        verify(documentContentVersionRepository).updateMimeType(docId, "application/pdf");
    }

    @Test
    void updateMimeType_shouldMarkAsUpdatedWhenDetectionFails() {
        UUID docId = UUID.randomUUID();
        DocumentContentVersion version = new DocumentContentVersion();
        version.setMimeType("application/octet-stream");

        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.of(version));
        when(mimeTypeDetectionService.detectMimeType(docId)).thenReturn(null);

        documentContentVersionService.updateMimeType(docId);

        // Should fall back to existing mime type
        verify(documentContentVersionRepository).updateMimeType(docId, "application/octet-stream");
    }

    @Test
    void updateMimeType_shouldDoNothingIfDocumentNotFound() {
        UUID docId = UUID.randomUUID();
        when(documentContentVersionRepository.findById(docId)).thenReturn(Optional.empty());

        documentContentVersionService.updateMimeType(docId);

        verify(mimeTypeDetectionService, never()).detectMimeType(docId);
        verify(documentContentVersionRepository, never()).updateMimeType(any(), any());
    }

}
