package uk.gov.hmcts.dm.functional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.blob.BlobInfo;
import uk.gov.hmcts.dm.blob.BlobReader;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.hamcrest.Matchers.equalTo;
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
    @Ignore
    public void uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles() throws IOException {
        uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(getVideo465mbId(), ".mp4", "video/mp4",
            (file, mimeType) -> {
                try {
                    uploadWhitelistedLargeFileThenDownload(file, mimeType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(getVideo625mbId(), ".mp4", "video/mp4",
                this::uploadingFileThrowsValidationSizeErrorMessage);
    }

    private void uploadAndDownLoadLargeFilesFromBlobStoreDmStoreFiles(String fileName, String fileExtension, String mimeType,
                                                                      BiConsumer<File, String> upload) throws IOException {
        assumeNotNull(blobReader);
        Optional<BlobInfo> mayBeBlobInfo = blobReader.retrieveBlobToProcess(fileName);
        if (mayBeBlobInfo.isPresent()) {
            var blobInfo = mayBeBlobInfo.get();
            var blobClient = blobInfo.getBlobClient();
            Path path = Files.createTempFile(fileName, fileExtension, ATTRIBUTE);
            try (var blobInputStream = blobClient.openInputStream()) {
                byte[] fileContent = blobInputStream.readAllBytes();
                blobInfo.releaseLease();

                Files.write(path, fileContent);
                File file = path.toFile();
                upload.accept(file, mimeType);
            } catch (Exception e) {
                log.error("Error in processing file : {}", fileName, e);
                fail(String.join(" : ",
                        "Error in processing file",
                        fileName,
                        ExceptionUtils.getStackTrace(e))
                );
            }
        } else {
            fail("File not found : " + fileName);
        }
    }

    @Test
    public void uploadLargeFilesFromBlobStoreDmStoreFiles() {
        streamBlobToUpload(getVideo465mbId(), "video/mp4");
        //streamBlobToUpload(getVideo625mbId(), "video/mp4");
    }

    private void streamBlobToUpload(String fileName,String mimeType) {
        assumeNotNull(blobReader);
        Optional<BlobInfo> mayBeBlobInfo = blobReader.retrieveBlobToProcess(fileName);
        if (mayBeBlobInfo.isPresent()) {
            var blobInfo = mayBeBlobInfo.get();
            var blobClient = blobInfo.getBlobClient();
            uploadWhitelistedLargeFile(blobClient.openInputStream(), fileName, mimeType);
            blobInfo.releaseLease();
        } else {
            fail("File not found : " + fileName);
        }
    }

    private void uploadWhitelistedLargeFile(InputStream inputStream, String fileName, String mimeType) {
        givenRequest(getCitizen())
            .multiPart("files", fileName, inputStream, mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(fileName))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo("caseworker"))
            .body("_embedded.documents[0].roles[1]", equalTo("citizen"))
            .when()
            .post("/documents");
    }
}
