package uk.gov.hmcts.dm.service;


import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.CantCreateBlobException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Service
public class AzureBlobService {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    public void uploadFile(MultipartFile file) {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
            CloudFileClient fileClient = storageAccount.createCloudFileClient();
            CloudFileShare share = fileClient.getShareReference("dem-test");
            if (share.createIfNotExists()) {
                CloudFileDirectory rootDir = share.getRootDirectoryReference();
                CloudFile cloudFile = rootDir.getFileReference(file.getOriginalFilename());
                cloudFile.upload(file.getInputStream(), file.getSize());

            }
        } catch (URISyntaxException | InvalidKeyException e) {
            throw new CantCreateBlobException(e);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public delete() {

    }


}
