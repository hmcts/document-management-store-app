package uk.gov.hmcts.dm.service;


import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.AzureBlobServiceException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

@Service
public class AzureBlobService {

    @Autowired
    private AzureFileClient azureFileClient;

    public DocumentContentVersion uploadFile(StoredDocument storedDocument, MultipartFile file, String creatorId) {

        try {

            UUID uuid = UUID.randomUUID();
            CloudFile cloudFile = azureFileClient.getCloudFile(uuid);
            cloudFile.upload(file.getInputStream(), file.getSize());

            return new DocumentContentVersion(
                uuid,
                storedDocument,
                file,
                creatorId);

        } catch (URISyntaxException | StorageException | IOException e) {
            throw new AzureBlobServiceException(e);
        }
    }

    public void delete(DocumentContentVersion documentContentVersion) {
        try {
            CloudFile cloudFile = azureFileClient.getCloudFile(documentContentVersion.getId());
            cloudFile.delete();
        } catch (URISyntaxException | StorageException e) {
            throw new AzureBlobServiceException(e);
        }
    }

    public void streamBinary(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        try {
            CloudFile cloudFile = azureFileClient.getCloudFile(documentContentVersion.getId());
            cloudFile.download(outputStream);
        } catch (URISyntaxException | StorageException e) {
            throw new AzureBlobServiceException(e);
        }
    }

}
