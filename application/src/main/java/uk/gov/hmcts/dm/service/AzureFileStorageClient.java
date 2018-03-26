package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
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

@Service(value = "azureFileStorageClient")
@Profile("azureStorage")
public class AzureFileStorageClient implements FileStorageClient {

    @Autowired
    private CloudFileShare cloudFileShare;

    @PostConstruct
    void init() throws StorageException {
        cloudFileShare.createIfNotExists();
    }

    @Override
    public void uploadFile(UUID uuid, MultipartFile multipartFile) {
        try {
            CloudFile cloudFile = getCloudFile(uuid);
            cloudFile.upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void streamFileContent(UUID uuid, OutputStream outputStream) {
        try {
            CloudFile cloudFile = getCloudFile(uuid);
            cloudFile.download(outputStream);
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void deleteFile(UUID uuid) {
        try {
            CloudFile cloudFile = getCloudFile(uuid);
            cloudFile.delete();
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException(e);
        }
    }

    private CloudFile getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudFileShare.getRootDirectoryReference().getFileReference(uuid.toString());
    }
}
