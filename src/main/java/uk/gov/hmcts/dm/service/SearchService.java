package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

@Service
public class SearchService {

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    public Page<StoredDocument> findStoredDocumentsByMetadata(
            @NonNull MetadataSearchCommand metadataSearchCommand, @NonNull Pageable pageable) {
        return storedDocumentRepository.findAllByMetadata(metadataSearchCommand, pageable);
    }

    public Page<StoredDocument> findStoredDocumentsByCreator(@NonNull String creator, @NonNull Pageable pageable) {
        return storedDocumentRepository.findByCreatedBy(creator, pageable);
    }

}
