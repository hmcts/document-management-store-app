package uk.gov.hmcts.dm.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
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
        InputStream io = null;
        try {
            io = documentContentVersion.getDocumentContent().getData().getBinaryStream();
            IOUtils.copy(io, outputStream);
        } catch (Exception e) {
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        } finally {
            IOUtils.closeQuietly(io);
        }
    }

    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        return storedDocumentRepository
                    .findByIdAndDeleted(id, false)
                    .map(StoredDocument::getMostRecentDocumentContentVersion);
    }

}
