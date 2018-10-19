package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.security.core.token.Sha512DigestUtils.shaHex;
import static uk.gov.hmcts.dm.domain.AuditActions.MIGRATED;

@Service
@Transactional
@Slf4j
public class BlobStorageMigrationService {

    private final CloudBlobContainer cloudBlobContainer;
    private final AuditEntryService auditEntryService;
    private final DocumentContentVersionService documentContentVersionService;
    private final StoredDocumentService storedDocumentService;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageMigrationService(CloudBlobContainer cloudBlobContainer,
                                       AuditEntryService auditEntryService,
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
        final DocumentContentVersion documentContentVersion = getDocumentContentVersion(documentId, versionId);
        migrateDocumentContentVersion(documentContentVersion);
    }

    private void migrateDocumentContentVersion(DocumentContentVersion documentContentVersion) {
        if (isBlank(documentContentVersion.getContentChecksum())) {
            log.info("Migrate DocumentContentVersion {}", documentContentVersion.getId());
            uploadBinaryStream(documentContentVersion);
            // we cannot use documentContentVersionRepository.save
            // because { @link ByteWrappingBlobType#replace} is not implemented
            documentContentVersionRepository.updateContentUriAndContentCheckSum(documentContentVersion.getId(),
                                                                                documentContentVersion.getContentUri(),
                                                                                documentContentVersion.getContentChecksum());
            auditEntryService.createAndSaveEntry(documentContentVersion, MIGRATED);
        }
    }

    private DocumentContentVersion getDocumentContentVersion(final @NotNull UUID documentId,
                                                             final @NotNull UUID versionId) {
        // Sonar fails us if we use orElseThrow
        if (!storedDocumentService.findOneWithBinaryData(documentId).isPresent()) {
            throw new DocumentNotFoundException(documentId);
        }

        return Optional
            .ofNullable(documentContentVersionService.findOne(versionId))
            .orElseThrow(() -> new DocumentContentVersionNotFoundException(versionId));
    }

    private void uploadBinaryStream(DocumentContentVersion dcv) {
        try {
            CloudBlockBlob cloudBlockBlob = getCloudFile(dcv.getId());
            Blob data = dcv.getDocumentContent().getData();
            final byte[] bytes = IOUtils.toByteArray(data.getBinaryStream());
            cloudBlockBlob.upload(new ByteArrayInputStream(bytes), dcv.getSize());
            dcv.setContentUri(cloudBlockBlob.getUri().toString());
            final String checksum = shaHex(bytes);
            dcv.setContentChecksum(checksum);
            log.debug("Uploaded data to {} Size = {} checksum = {}", cloudBlockBlob.getUri(), dcv.getSize(), checksum);

            final ByteArrayOutputStream checksumStream = new ByteArrayOutputStream();
            cloudBlockBlob.download(checksumStream);
            if (! checksum.equals(shaHex(checksumStream.toByteArray()))) {
                throw new FileStorageException(dcv.getStoredDocument().getId(), dcv.getId());
            }
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e, dcv.getStoredDocument().getId(), dcv.getId());
        } catch (SQLException e) {
            log.error("Exception with Document Content Version {}", dcv.getId(), e);
            throw new CantReadDocumentContentVersionBinaryException(e, dcv);
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
