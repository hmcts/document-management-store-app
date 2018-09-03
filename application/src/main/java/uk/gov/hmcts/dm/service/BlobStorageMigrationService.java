package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;

@Service
@Transactional
public class BlobStorageMigrationService {

    private final CloudBlobContainer cloudBlobContainer;
    private final AuditEntryService auditEntryService;
    private final DocumentContentVersionService documentContentVersionService;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageMigrationService(CloudBlobContainer cloudBlobContainer, AuditEntryService auditEntryService,
                                       DocumentContentVersionRepository documentContentVersionRepository,
                                       DocumentContentVersionService documentContentVersionService) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.auditEntryService = auditEntryService;
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.documentContentVersionService = documentContentVersionService;
    }

    // TODO check READ/UPDATE access here
    //    @PreAuthorize("hasPermission(#versionId, 'uk.gov.hmcts.dm.domain.DocumentContentVersion', 'READ')")
    public void migrateDocumentContentVersion(@NotNull UUID versionId) {
        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);
        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(versionId);
        }
        uploadBinaryStream(documentContentVersion);
        documentContentVersionRepository.save(documentContentVersion);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.UPDATED);
    }

    private void uploadBinaryStream(DocumentContentVersion doc) {
        try {
            // TODO check if multiple uploads with the same Id causes an issue
            CloudBlockBlob blob = getCloudFile(doc.getId());
            blob.upload(doc.getDocumentContent().getData().getBinaryStream(), doc.getSize());
            doc.setContent_uri(blob.getUri().toString());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e);
        } catch (SQLException e) {
            throw new CantReadDocumentContentVersionBinaryException(e, doc);
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
