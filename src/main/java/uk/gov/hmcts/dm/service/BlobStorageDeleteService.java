package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;


@Slf4j
@Service
public class BlobStorageDeleteService {

    private BlobContainerClient cloudBlobContainer;
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageDeleteService(BlobContainerClient cloudBlobContainer,
                                    DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion) {
        log.info(
            "Deleting document {}, StoredDocument {} from Azure Blob Storage...",
            documentContentVersion.getId(),
            documentContentVersion.getStoredDocument().getId()
        );

        BlockBlobClient blob = cloudBlobContainer.getBlobClient(documentContentVersion.getId().toString()).getBlockBlobClient();
        try {
            Response res = blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
            if (res.getStatusCode() != 202 && res.getStatusCode() != 404) {
                log.info(
                    "Deleting document {} failed. Response status code {}",
                    documentContentVersion.getId(),
                    res.getStatusCode()
                );
            } else {
                documentContentVersionRepository.updateContentUriAndContentCheckSum(
                    documentContentVersion.getId(), null, null);
            }
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                log.info(
                    "Blob not found for deletion {}, StoredDocument {}",
                    documentContentVersion.getId(),
                    documentContentVersion.getStoredDocument().getId()
                );
                documentContentVersionRepository.updateContentUriAndContentCheckSum(
                    documentContentVersion.getId(), null, null);
            } else {
                log.info(
                    "Deleting document failed {},status {} from Azure Blob Storage.",
                    documentContentVersion.getId(),
                    e.getStatusCode(),
                    e
                );
            }
        }
    }

}
