package uk.gov.hmcts.dm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class DocumentContentVersionService {


    private final DocumentContentVersionRepository documentContentVersionRepository;


    private final StoredDocumentRepository storedDocumentRepository;

    public DocumentContentVersionService(
        DocumentContentVersionRepository documentContentVersionRepository,
        StoredDocumentRepository storedDocumentRepository
    ) {
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.storedDocumentRepository = storedDocumentRepository;
    }

    public Optional<DocumentContentVersion> findById(UUID id) {
        return documentContentVersionRepository.findById(id);
    }

    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        return storedDocumentRepository
                    .findByIdAndDeleted(id, false)
                    .map(StoredDocument::getMostRecentDocumentContentVersion);
    }

}
