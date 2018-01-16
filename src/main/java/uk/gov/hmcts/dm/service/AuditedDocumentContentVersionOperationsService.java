package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Created by pawel on 28/07/2017.
 */
@Service
@Transactional
public class AuditedDocumentContentVersionOperationsService {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private DocumentThumbnailService documentThumbnailService;

    @Autowired
    private AuditEntryService auditEntryService;

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public void readDocumentContentVersionBinary(@NotNull DocumentContentVersion documentContentVersion) {
        documentContentVersionService.streamDocumentContentVersion(documentContentVersion);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public ResponseEntity<InputStreamResource> readDocumentContentVersionThumbnail(@NotNull DocumentContentVersion documentContentVersion) {
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
        return documentThumbnailService.generateThumbnail(documentContentVersion);
    }


    @PreAuthorize("hasPermission(#versionId, 'uk.gov.hmcts.dm.domain.DocumentContentVersion', 'READ')")
    public DocumentContentVersion readDocumentContentVersion(@NotNull UUID versionId) {
        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);
        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(String.format("ID: %s", versionId.toString()));
        }
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
        return documentContentVersion;
    }

}
