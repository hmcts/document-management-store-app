package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import javax.validation.constraints.NotNull;
import java.io.OutputStream;
import java.util.UUID;

@Service
@Transactional
public class AuditedDocumentContentVersionOperationsService {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private DocumentThumbnailService documentThumbnailService;

    @Autowired
    private BlobStorageReadService blobStorageReadService;

    @Autowired
    private AuditEntryService auditEntryService;

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public void readDocumentContentVersionBinaryFromBlobStore(DocumentContentVersion documentContentVersion,
                                                  OutputStream outputStream) {
        blobStorageReadService.loadBlob(documentContentVersion, outputStream);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public Resource readDocumentContentVersionThumbnail(@NotNull DocumentContentVersion documentContentVersion) {
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
        return documentThumbnailService.generateThumbnail(documentContentVersion);
    }

    @PreAuthorize("hasPermission(#versionId, 'uk.gov.hmcts.dm.domain.DocumentContentVersion', 'READ')")
    public DocumentContentVersion readDocumentContentVersion(@NotNull UUID versionId) {
        return documentContentVersionService.findById(versionId)
            .filter(documentContentVersion -> !documentContentVersion.getStoredDocument().isDeleted())
            .map(documentContentVersion -> {
                auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
                return documentContentVersion;
            }).orElseThrow(() -> new DocumentContentVersionNotFoundException(versionId));
    }

}
