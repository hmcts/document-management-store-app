package uk.gov.hmcts.dm.service;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service to detect the MIME type of a document stored in blob storage.
 */
@Service
public class MimeTypeDetectionService {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeDetectionService.class);
    private static final int MAX_BYTES_TO_READ = 2 * 1024 * 1024; // 2 MB is sufficient for Tika to detect type

    private final BlobStorageReadService blobStorageReadService;

    public MimeTypeDetectionService(BlobStorageReadService blobStorageReadService) {
        this.blobStorageReadService = blobStorageReadService;
    }

    /**
     * Detects the MIME type of a document version by reading the first few bytes from its blob.
     *
     * @param documentVersionId The UUID of the document version.
     * @return The detected MIME type as a String, or null if detection fails.
     */
    public String detectMimeType(UUID documentVersionId) {
        log.debug("Attempting to detect MIME type for document version ID: {}", documentVersionId);
        try (InputStream inputStream = blobStorageReadService.getInputStream(documentVersionId);
             BoundedInputStream limitedStream = BoundedInputStream.builder()
                 .setInputStream(inputStream)
                 .setMaxCount(MAX_BYTES_TO_READ)
                 .get()) {

            Tika tika = new Tika();
            Metadata metadata = new Metadata();
            String mimeType = tika.detect(limitedStream, metadata);
            log.info("Detected MIME type for {} as: {}", documentVersionId, mimeType);
            return mimeType;

        } catch (IOException e) {
            log.error("Failed to read blob stream for MIME type detection on document version {}",
                documentVersionId);
            return null;
        } catch (Exception e) {
            log.error("An unexpected error occurred during MIME type detection for document version {}",
                documentVersionId);
            return null;
        }
    }
}
