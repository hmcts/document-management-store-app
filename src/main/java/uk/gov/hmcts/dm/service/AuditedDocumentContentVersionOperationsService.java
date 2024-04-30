package uk.gov.hmcts.dm.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;

import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
public class AuditedDocumentContentVersionOperationsService {

    private final DocumentContentVersionService documentContentVersionService;

    private final BlobStorageReadService blobStorageReadService;

    private final AuditEntryService auditEntryService;

    @Autowired
    public AuditedDocumentContentVersionOperationsService(DocumentContentVersionService documentContentVersionService,
                                                          BlobStorageReadService blobStorageReadService,
                                                          AuditEntryService auditEntryService) {
        this.documentContentVersionService = documentContentVersionService;
        this.blobStorageReadService = blobStorageReadService;
        this.auditEntryService = auditEntryService;
    }

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public void readDocumentContentVersionBinaryFromBlobStore(DocumentContentVersion documentContentVersion,
                                                              HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        blobStorageReadService.loadBlob(documentContentVersion, request, response);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
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
