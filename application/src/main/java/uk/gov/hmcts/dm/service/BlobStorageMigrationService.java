package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional
public class BlobStorageMigrationService {

    private final CloudBlobContainer cloudBlobContainer;
    private final AuditEntryService auditEntryService;
    private final StoredDocumentService storedDocumentService;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageMigrationService(CloudBlobContainer cloudBlobContainer,
                                       AuditEntryService auditEntryService,
                                       DocumentContentVersionRepository documentContentVersionRepository,
                                       StoredDocumentService storedDocumentService) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.auditEntryService = auditEntryService;
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.storedDocumentService = storedDocumentService;
    }

    public void migrateDocumentContentVersion(@NotNull UUID documentId, @NotNull UUID versionId) {

        final DocumentContentVersion documentContentVersion = getDocumentContentVersion(documentId, versionId);

        if (isBlank(documentContentVersion.getContentUri())) {
            uploadBinaryStream(documentId, documentContentVersion);
            // we cannot use documentContentVersionRepository.save
            // because { @link ByteWrappingBlobType#replace} is not implemented
            documentContentVersionRepository.update(documentContentVersion.getId(),
                documentContentVersion.getContentUri());
            auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.UPDATED);
        }
    }

    private DocumentContentVersion getDocumentContentVersion(final @NotNull UUID documentId,
                                                             final @NotNull UUID versionId) {
        // Sonar fails us if we use orElseThrow
        if (!storedDocumentService.findOneWithBinaryData(documentId).isPresent()) {
            throw new DocumentNotFoundException(documentId);
        }

        return Optional
            .ofNullable(documentContentVersionRepository.findOne(versionId))
            .orElseThrow(() -> new DocumentContentVersionNotFoundException(versionId));
    }

    private void uploadBinaryStream(UUID documentId, DocumentContentVersion dcv) {
        try {
            CloudBlockBlob cloudBlockBlob = getCloudFile(getUniqueBlobId(dcv));
            Blob data = dcv.getDocumentContent().getData();
            cloudBlockBlob.upload(data.getBinaryStream(), dcv.getSize());
            dcv.setContentUri(cloudBlockBlob.getUri().toString());
            dcv.setContentChecksum(Sha512DigestUtils.shaHex(data.getBytes(1, (int)data.length())));
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e, documentId, dcv.getId());
        } catch (SQLException e) {
            throw new CantReadDocumentContentVersionBinaryException(e, dcv);
        }
    }

    private String getUniqueBlobId(final DocumentContentVersion documentContentVersion) {
        return format("%s-%s-%s",
                      randomUUID(),
                      defaultIfNull(documentContentVersion.getStoredDocument().getId(), randomUUID()),
                      defaultIfNull(documentContentVersion.getId(), randomUUID()));
    }

    private CloudBlockBlob getCloudFile(String id) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(id);
    }
}
