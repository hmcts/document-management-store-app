package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class DocumentContentVersionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentContentVersionService.class);

    private final DocumentContentVersionRepository documentContentVersionRepository;
    private final StoredDocumentRepository storedDocumentRepository;
    private final MimeTypeDetectionService mimeTypeDetectionService; // New dependency

    @Autowired
    public DocumentContentVersionService(DocumentContentVersionRepository documentContentVersionRepository,
                                         StoredDocumentRepository storedDocumentRepository,
                                         MimeTypeDetectionService mimeTypeDetectionService) { // Injected here
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.storedDocumentRepository = storedDocumentRepository;
        this.mimeTypeDetectionService = mimeTypeDetectionService;
    }

    public Optional<DocumentContentVersion> findById(UUID id) {
        return documentContentVersionRepository.findById(id);
    }

    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        return storedDocumentRepository
            .findByIdAndDeleted(id, false)
            .map(StoredDocument::getMostRecentDocumentContentVersion);
    }

    /**
     * Updates the MIME type for a single DocumentContentVersion.
     * This method is called by the MimeTypeUpdateTask batch job.
     *
     * @param documentVersionId The UUID of the document version to update.
     */
    public void updateMimeType(UUID documentVersionId) {
        try {
            log.debug("Processing MIME type update for ID: {}", documentVersionId);

            String detectedMimeType = mimeTypeDetectionService.detectMimeType(documentVersionId);

            if (detectedMimeType == null) {
                log.warn("Could not detect MIME type for {}. Skipping update.", documentVersionId);
                // We will still mark it as "updated" to prevent it from being picked up again.
                documentContentVersionRepository.findById(documentVersionId).ifPresent(version -> {
                    version.setMimeTypeUpdated(true);
                });
                return;
            }

            DocumentContentVersion version = documentContentVersionRepository.findById(documentVersionId)
                .orElseThrow(() -> new RuntimeException("DocumentContentVersion not found: " + documentVersionId));

            if (!Objects.equals(version.getMimeType(), detectedMimeType)) {
                log.info("Updating MIME type for document {}. Old: [{}], New: [{}].",
                    documentVersionId, version.getMimeType(), detectedMimeType);
                version.setMimeType(detectedMimeType);
            } else {
                log.info("Detected MIME type for {} is the same as existing one [{}]. No update needed.",
                    documentVersionId, detectedMimeType);
            }

            version.setMimeTypeUpdated(true);

        } catch (Exception e) {
            log.error("Error updating MIME type for document {}: {}", documentVersionId, e.getMessage(), e);
            // FOr now not re-throwing
        }
    }
}

