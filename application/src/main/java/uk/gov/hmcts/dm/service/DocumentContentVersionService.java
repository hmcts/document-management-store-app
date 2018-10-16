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
import java.io.OutputStream;
import java.util.UUID;

@Transactional
@Service
public class DocumentContentVersionService {

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    public DocumentContentVersion findOne(UUID id) {
        return documentContentVersionRepository.findOne(id);
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

    public DocumentContentVersion findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        StoredDocument storedDocument = storedDocumentRepository.findOne(id);
        return storedDocument != null ? storedDocument.getMostRecentDocumentContentVersion() : null;
    }

}
