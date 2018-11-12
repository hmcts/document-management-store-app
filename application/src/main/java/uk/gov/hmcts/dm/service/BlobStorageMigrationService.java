package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.BatchMigrationAuditEntry;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.MigrateEntry;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.MigrateEntryRepository;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.security.core.token.Sha512DigestUtils.shaHex;
import static uk.gov.hmcts.dm.domain.AuditActions.MIGRATED;

@Service
@Transactional
@Slf4j
public class BlobStorageMigrationService {

    protected static final String NO_CONTENT_FOUND = "CONTENT NOT FOUND";
    private final CloudBlobContainer cloudBlobContainer;
    private final StoredDocumentService storedDocumentService;
    private final DocumentContentVersionRepository documentContentVersionRepository;
    private final MigrateEntryRepository auditEntryRepository;
    private final BatchMigrationTokenService batchMigrationTokenService;
    private final BatchMigrationAuditEntryService batchMigrationAuditEntryService;

    @Value("${blobstore.migrate.default.batchSize:5}")
    protected int defaultBatchSize;

    @Autowired
    public BlobStorageMigrationService(CloudBlobContainer cloudBlobContainer,
                                       DocumentContentVersionRepository documentContentVersionRepository,
                                       StoredDocumentService storedDocumentService,
                                       MigrateEntryRepository auditEntryRepository,
                                       BatchMigrationTokenService batchMigrationTokenService,
                                       BatchMigrationAuditEntryService batchMigrationAuditEntryService) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.storedDocumentService = storedDocumentService;
        this.auditEntryRepository = auditEntryRepository;
        this.batchMigrationTokenService = batchMigrationTokenService;
        this.batchMigrationAuditEntryService = batchMigrationAuditEntryService;
    }

    public void migrateDocumentContentVersion(@NotNull UUID documentId, @NotNull UUID versionId) {
        final DocumentContentVersion documentContentVersion = getDocumentContentVersion(documentId, versionId);
        migrateDocumentContentVersion(documentContentVersion);
    }

    public BatchMigrateProgressReport batchMigrate(String authToken, final Integer limit, final Boolean mockRun) {
        synchronized (this) {
            Integer actualLimit = defaultIfNull(limit, defaultBatchSize);
            Boolean actualRun = defaultIfNull(mockRun, false);
            BatchMigrationAuditEntry audit = batchMigrationAuditEntryService.createAuditEntry(authToken,
                                                                                              actualLimit,
                                                                                              actualRun);
            batchMigrationTokenService.checkAuthToken(authToken);
            final BatchMigrateProgressReport report = batchMigrate(actualLimit, actualRun);
            batchMigrationAuditEntryService.save(audit, report);
            return report;
        }
    }

    private BatchMigrateProgressReport batchMigrate(int limit, boolean mockRun) {
        final long start = currentTimeMillis();

        MigrateProgressReport before = getMigrateProgressReport();

        final List<DocumentContentVersion> dcvList = documentContentVersionRepository
            .findByContentChecksumIsNullAndDocumentContentIsNotNull(
            new PageRequest(0, limit, DESC, "createdOn"));

        if (!mockRun) {
            dcvList.forEach(this::migrateDocumentContentVersion);
        }

        MigrateProgressReport after = getMigrateProgressReport();

        final long finish = currentTimeMillis();
        return new BatchMigrateProgressReport(before, dcvList, after, Duration.ofMillis(finish - start));
    }

    public MigrateProgressReport getMigrateProgressReport() {
        return new MigrateProgressReport(documentContentVersionRepository.countByContentChecksumIsNull(),
                                         documentContentVersionRepository.countByContentChecksumIsNotNull());
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
            // For some reason,
            // auditEntryService.createAndSaveEntry
            // only creates one database entry; hence use auditEntryRepository.saveAndFlush directly
            auditEntryRepository.saveAndFlush(newAuditEntry(documentContentVersion));
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

    private void uploadBinaryStream(DocumentContentVersion dcv) {
        try {
            if (null == dcv.getDocumentContent()) {
                log.warn("Document Content Version (id={} has no content", dcv.getId());
                dcv.setContentChecksum(NO_CONTENT_FOUND);
            } else {
                CloudBlockBlob cloudBlockBlob = getCloudFileReference(dcv.getId());
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
            }
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e, dcv.getStoredDocument().getId(), dcv.getId());
        } catch (SQLException e) {
            log.error("Exception with Document Content Version {}", dcv.getId(), e);
            throw new CantReadDocumentContentVersionBinaryException(e, dcv);
        }
    }

    private CloudBlockBlob getCloudFileReference(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }

    private MigrateEntry newAuditEntry(DocumentContentVersion documentContentVersion) {
        return new MigrateEntry("Migrate content", MIGRATED, documentContentVersion, "Batch Migration Service");
    }
}
