package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
@Transactional
public class BlobStorageMigrationService {

    private final CloudBlobContainer cloudBlobContainer;
    private final AuditEntryService auditEntryService;
    private final DocumentContentVersionService documentContentVersionService;
    private final StoredDocumentService storedDocumentService;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageMigrationService(CloudBlobContainer cloudBlobContainer, AuditEntryService auditEntryService,
                                       DocumentContentVersionRepository documentContentVersionRepository,
                                       DocumentContentVersionService documentContentVersionService,
                                       StoredDocumentService storedDocumentService) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.auditEntryService = auditEntryService;
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.documentContentVersionService = documentContentVersionService;
        this.storedDocumentService = storedDocumentService;
    }

    public void migrateDocumentContentVersion(@NotNull UUID documentId, @NotNull UUID versionId) {
        StoredDocument storedDocument = storedDocumentService.findOne(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);
        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(versionId);
        }

        if (isBlank(documentContentVersion.getContentUri())) {
            uploadBinaryStream(documentId, documentContentVersion);
            // we cannot use documentContentVersionRepository.save
            // because { @link ByteWrappingBlobType#replace} is not implemented
            documentContentVersionRepository.update(documentContentVersion.getId(),
                documentContentVersion.getContentUri());
            auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.UPDATED);
        }
    }

    private void uploadBinaryStream(UUID documentId, DocumentContentVersion doc) {
        try {
            CloudBlockBlob blob = getCloudFile(doc.getId());
            blob.upload(doc.getDocumentContent().getData().getBinaryStream(), doc.getSize());
            doc.setContentUri(blob.getUri().toString());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e, documentId, doc.getId());
        } catch (SQLException e) {
            throw new CantReadDocumentContentVersionBinaryException(e, doc);
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
