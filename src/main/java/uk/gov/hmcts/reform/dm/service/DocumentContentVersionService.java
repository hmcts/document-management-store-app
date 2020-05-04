package uk.gov.hmcts.reform.dm.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.reform.dm.domain.StoredDocument;
import uk.gov.hmcts.reform.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.reform.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.reform.dm.repository.StoredDocumentRepository;

import javax.validation.constraints.NotNull;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class DocumentContentVersionService {

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    public Optional<DocumentContentVersion> findById(UUID id) {
        return documentContentVersionRepository.findById(id);
    }

    public void streamDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion, @NotNull OutputStream outputStream) {
        if (documentContentVersion.getDocumentContent() == null || documentContentVersion.getDocumentContent().getData() == null) {
            throw new CantReadDocumentContentVersionBinaryException("File content is null", documentContentVersion);
        }
        try {
            IOUtils.copy(documentContentVersion.getDocumentContent().getData().getBinaryStream(), outputStream);
        } catch (Exception e) {
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        }
    }

    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        return storedDocumentRepository
                    .findByIdAndDeleted(id, false)
                    .map(StoredDocument::getMostRecentDocumentContentVersion);
    }

}
