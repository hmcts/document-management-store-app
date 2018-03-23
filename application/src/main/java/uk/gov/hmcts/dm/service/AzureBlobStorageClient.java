package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.FileStorageException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

@Service(value = "azureBlobStorageClient")
@Profile("azureStorage")
public class AzureBlobStorageClient implements FileStorageClient {

    @Autowired
    private CloudBlobContainer cloudBlobContainer;

    @PostConstruct
    void init() throws StorageException {
        cloudBlobContainer.createIfNotExists();
    }

    @Override
    public void uploadFile(UUID uuid, MultipartFile multipartFile) {
        try {
            getCloudFile(uuid).upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void streamFileContent(UUID uuid, OutputStream outputStream) {
        try {
            getCloudFile(uuid).download(outputStream);
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void deleteFile(UUID uuid) {
        try {
            getCloudFile(uuid).delete();
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException(e);
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
