package uk.gov.hmcts.dm.functional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.blob.BlobInfo;
import uk.gov.hmcts.dm.blob.BlobReader;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

@Slf4j
public class LargeMultiMediaUploadIT extends BaseIT {
    private static final FileAttribute<Set<PosixFilePermission>> ATTRIBUTE = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    @Autowired(required = false)
    private BlobReader blobReader;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles() throws IOException {
        uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(getVideo465mbId(), ".mp4", "video/mp4");
        uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(getVideo625mbId(), ".mp4", "video/mp4");
    }

    private void uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(String fileName, String fileExtension, String mimeType) throws IOException {
        assumeNotNull(blobReader);
        Optional<BlobInfo> mayBeBlobInfo = blobReader.retrieveBlobToProcess(fileName);
        if (mayBeBlobInfo.isPresent()) {
            var blobInfo = mayBeBlobInfo.get();
            var blobClient = blobInfo.getBlobClient();
            String blobName = blobClient.getBlobName();
            Path path = Files.createTempFile(fileName, fileExtension, ATTRIBUTE);
            try (var blobInputStream = blobClient.openInputStream()) {
                byte[] fileContent = blobInputStream.readAllBytes();
                blobInfo.releaseLease();

                Files.write(path, fileContent);
                File file = path.toFile();
                uploadWhitelistedLargeFileThenDownload(fileName, mimeType,
                    (doc, metadataKey) -> file);
            } catch (Exception e) {
                log.error("Error in processing blob : {}", blobName, e);
                fail(ExceptionUtils.getStackTrace(e));
            }
        } else {
            assertTrue("File not found : " + fileName, mayBeBlobInfo.isPresent());
        }
    }
}
