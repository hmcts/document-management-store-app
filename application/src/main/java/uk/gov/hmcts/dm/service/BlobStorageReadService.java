package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.OutputStream;
import java.net.URISyntaxException;

@Service
@Transactional
public class BlobStorageReadService {

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public BlobStorageReadService(CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public void loadBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        try {
            CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString());
            blob.download(outputStream);
        } catch (URISyntaxException | StorageException e) {
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        }
    }
}
