package uk.gov.hmcts.dm.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * Created by pawel on 26/05/2017.
 */
@Transactional
@Service
public class DocumentContentVersionService {

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    @Autowired
    @Setter
    private HttpServletResponse response;

    @Setter
    @Getter
    private int streamBufferSize = 5000000;

    public DocumentContentVersion findOne(UUID id) {
        return documentContentVersionRepository.findOne(id);
    }

    public void streamDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion) {
        if (documentContentVersion.getDocumentContent() == null || documentContentVersion.getDocumentContent().getData() == null) {
            throw new CantReadDocumentContentVersionBinaryException("File content is null", documentContentVersion);
        }
        try {
            response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
            response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
            response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"" + documentContentVersion.getOriginalDocumentName() + "\"");
            IOUtils.copy(documentContentVersion.getDocumentContent().getData().getBinaryStream(), response.getOutputStream(), streamBufferSize);
        } catch (Exception e) {
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        }
    }

    public DocumentContentVersion findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        StoredDocument storedDocument = storedDocumentRepository.findOne(id);
        return storedDocument != null ? storedDocument.getMostRecentDocumentContentVersion() : null;
    }

}
