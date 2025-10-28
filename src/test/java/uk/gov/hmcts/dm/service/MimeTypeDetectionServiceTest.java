package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MimeTypeDetectionServiceTest {

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @InjectMocks
    private MimeTypeDetectionService mimeTypeDetectionService;

    private UUID documentVersionId;

    @BeforeEach
    void setUp() {
        documentVersionId = UUID.randomUUID();
    }

    @Test
    void testDetectMimeTypeSuccess() {
        // Given
        String content = "this is some plain text content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        when(blobStorageReadService.getInputStream(documentVersionId)).thenReturn(inputStream);

        // When
        String detectedMimeType = mimeTypeDetectionService.detectMimeType(documentVersionId);

        // Then
        assertEquals("text/plain", detectedMimeType);
    }

    @Test
    void testDetectMimeTypeHandlesGenericException() {
        // Given
        when(blobStorageReadService.getInputStream(documentVersionId))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        String detectedMimeType = mimeTypeDetectionService.detectMimeType(documentVersionId);

        // Then
        assertNull(detectedMimeType);
    }

    @Test
    void testDetectMimeTypeHandlesIOException() {
        InputStream throwingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("IO error");
            }
        };

        when(blobStorageReadService.getInputStream(documentVersionId)).thenReturn(throwingStream);

        String detectedMimeType = mimeTypeDetectionService.detectMimeType(documentVersionId);

        assertNull(detectedMimeType);
    }

    @Test
    void testDetectMimeTypeForPdf() {
        // Given
        // A minimal PDF file header to simulate PDF content
        byte[] pdfBytes = "%PDF-1.4".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(pdfBytes);
        when(blobStorageReadService.getInputStream(any(UUID.class))).thenReturn(inputStream);

        // When
        String detectedMimeType = mimeTypeDetectionService.detectMimeType(UUID.randomUUID());

        // Then
        assertEquals("application/pdf", detectedMimeType);
    }
}

