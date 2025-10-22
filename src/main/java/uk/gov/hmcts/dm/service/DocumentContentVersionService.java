package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;
import java.util.UUID;

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

    @Transactional
    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        return storedDocumentRepository
            .findByIdAndDeleted(id, false)
            .map(StoredDocument::getMostRecentDocumentContentVersion);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMimeType(UUID documentVersionId) {
        log.info("Processing MIME type update for ID: {}", documentVersionId);

        Optional<DocumentContentVersion> versionOptional =
            documentContentVersionRepository.findById(documentVersionId);
        if (versionOptional.isEmpty()) {
            log.warn(
                "DocumentContentVersion not found during MIME type update: {}. Nothing to process.",
                documentVersionId
            );
            return;
        }

        DocumentContentVersion documentVersion = versionOptional.get();
        String detectedMimeType = mimeTypeDetectionService.detectMimeType(documentVersionId);

        if (detectedMimeType == null) {
            log.warn(
                "Could not detect MIME type for {}. Marking as processed to prevent retries.",
                documentVersionId
            );
            detectedMimeType = documentVersion.getMimeType();
        } else {
            log.info("Updating MIME type for document {}. Old: [{}], New: [{}].",
                documentVersionId, documentVersion.getMimeType(), detectedMimeType);
        }
        documentContentVersionRepository.updateMimeType(documentVersionId, detectedMimeType);

        log.info("documentVersion id:{}, mimeType:{}, isUpdated: {}",
            documentVersion.getId(),
            documentVersion.getMimeType(),
            documentVersion.isMimeTypeUpdated()
        );
    }
}

