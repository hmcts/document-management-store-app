package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import static java.lang.Boolean.TRUE;


@Slf4j
@Service
public class BlobStorageDeleteService {

    private BlobContainerClient cloudBlobContainer;

    @Autowired
    public BlobStorageDeleteService(BlobContainerClient cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion) {
        log.info(
            "Deleting document blob {}, StoredDocument {}",
            documentContentVersion.getId(),
            documentContentVersion.getStoredDocument().getId()
        );

        try {
            BlockBlobClient blob =
                cloudBlobContainer.getBlobClient(documentContentVersion.getId().toString()).getBlockBlobClient();
            if (TRUE.equals(blob.exists())) {
                Response<Void> res = blob.deleteWithResponse(
                    DeleteSnapshotsOptionType.INCLUDE, null, null, null);
                if (res.getStatusCode() != 202 && res.getStatusCode() != 404) {
                    log.info(
                        "Deleting document blob {} failed. Response status code {}",
                        documentContentVersion.getId(),
                        res.getStatusCode()
                    );
                    return;
                }
                log.info(
                    "Successfully deleted blob: {}, document {}, StoredDocument {}",
                    blob.getBlobUrl(),
                    documentContentVersion.getId(),
                    documentContentVersion.getStoredDocument().getId()
                );
            }
            documentContentVersion.setContentUri(null);
            documentContentVersion.setContentChecksum(null);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                log.info(
                    "Blob Not found for deletion {}, StoredDocument {}",
                    documentContentVersion.getId(),
                    documentContentVersion.getStoredDocument().getId()
                );
                documentContentVersion.setContentUri(null);
                documentContentVersion.setContentChecksum(null);
            } else {
                log.info(
                    "Deleting document blob failed {},status {}",
                    documentContentVersion.getId(),
                    e.getStatusCode(),
                    e
                );
            }
        }
    }

}
