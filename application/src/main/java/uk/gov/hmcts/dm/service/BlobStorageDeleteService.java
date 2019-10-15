package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.DeleteSnapshotsOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;

@Slf4j
@Service
public class BlobStorageDeleteService {

    private final CloudBlobContainer cloudBlobContainer;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageDeleteService(CloudBlobContainer cloudBlobContainer,
                                    DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    public void deleteDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion) {
        log.debug("Deleting document {} / version {} from Azure Blob Storage...",
            documentContentVersion.getStoredDocument().getId(), documentContentVersion.getId());

        try {

            CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString());
            if (!blob.deleteIfExists(DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null)) {
                log.info("Deleting document {} / version {} from Azure Blob Storage: Blob could not be found.");
            } else {
                documentContentVersionRepository.updateContentUriAndContentCheckSum(
                    documentContentVersion.getId(), null, null);
            }

        } catch (URISyntaxException | StorageException  e) {
            log.warn("Deleting document {} / version {} from Azure Blob Storage: FAILED",
                documentContentVersion.getStoredDocument().getId(), documentContentVersion.getId());
            throw new FileStorageException(e, documentContentVersion.getStoredDocument().getId(), documentContentVersion.getId());
        }
    }

}
