package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;


@Slf4j
@Service
public class BlobStorageDeleteService {

    private final BlobContainerClient cloudBlobContainer;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageDeleteService(BlobContainerClient cloudBlobContainer,
                                    DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    public void deleteDocumentContentVersion(@NotNull DocumentContentVersion documentContentVersion) {
        log.debug("Deleting document {} / version {} from Azure Blob Storage...",
            documentContentVersion.getStoredDocument().getId(), documentContentVersion.getId());

        BlockBlobClient blob = cloudBlobContainer.getBlobClient(documentContentVersion.getId().toString()).getBlockBlobClient();
        Response res = blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        if (res.getStatusCode() != 202) {
            log.info("Deleting document {} / version {} from Azure Blob Storage: Blob could not be found.",
                documentContentVersion.getId());
        } else {
            documentContentVersionRepository.updateContentUriAndContentCheckSum(
                documentContentVersion.getId(), null, null);
        }

    }

}
