package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    public Page<StoredDocument> findStoredDocumentsByMetadata(
            @NonNull MetadataSearchCommand metadataSearchCommand, @NonNull Pageable pageable) {
        return storedDocumentRepository.findAllByMetadata(metadataSearchCommand, pageable);
    }

    public List<StoredDocument> findStoredDocumentsIdsByCaseRef(
        @NonNull DeleteCaseDocumentsCommand deleteCaseDocumentsCommand) {

        final List<UUID> storedDocumentsIdsList =
                storedDocumentRepository.findAllByCaseRef(deleteCaseDocumentsCommand.getCaseRef());

        final List<StoredDocument> storedDocuments = storedDocumentsIdsList
                .stream()
                .map(StoredDocument::new)
                .collect(toList());

        return storedDocuments;
    }

    public Page<StoredDocument> findStoredDocumentsByCreator(@NonNull String creator, @NonNull Pageable pageable) {
        return storedDocumentRepository.findByCreatedBy(creator, pageable);
    }

}
