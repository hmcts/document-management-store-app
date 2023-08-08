package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.repository.DocumentDaoImpl;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
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

    @Autowired
    private SecurityUtilService securityUtilService;

    @Autowired
    private DocumentDaoImpl documentDao;

    @PreAuthorize("hasPermission(#documentContentVersion, 'READ')")
    public void readDocumentContentVersionBinaryFromBlobStore(DocumentContentVersion documentContentVersion,
                                                              HttpServletRequest request, HttpServletResponse response, UUID documentId)
        throws IOException {
//        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.READ);
        documentDao.createAndSaveDocumentContentVersionAuditEntry(documentContentVersion, securityUtilService.getUserId(),
            securityUtilService.getCurrentlyAuthenticatedServiceName(), AuditActions.READ, documentId);
        blobStorageReadService.loadBlob(documentContentVersion, request, response);

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
