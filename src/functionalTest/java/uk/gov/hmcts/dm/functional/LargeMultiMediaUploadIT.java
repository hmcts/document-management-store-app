package uk.gov.hmcts.dm.functional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.blob.BlobInfo;
import uk.gov.hmcts.dm.blob.BlobReader;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.InputStream;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.dm.functional.V1MimeTypes.VIDEO_MPEG_VALUE;

@Slf4j
public class LargeMultiMediaUploadIT extends BaseIT {

    @Autowired(required = false)
    private BlobReader blobReader;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void uploadLargeFilesFromBlobStoreDmStoreFiles() {
        streamBlobToUpload(getVideo260kbId(), VIDEO_MPEG_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
        streamBlobToUpload(getPdf1kbId(), APPLICATION_PDF_VALUE, this::uploadWhitelistedLargeFileSuccessfully);

        streamBlobToUpload(getVideo465mbId(), VIDEO_MPEG_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
        streamBlobToUpload(getPdf990mbId(), APPLICATION_PDF_VALUE, this::uploadWhitelistedLargeFileSuccessfully);

        streamBlobToUpload(getVideo625mbId(), VIDEO_MPEG_VALUE, this::uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage);
        //streamBlobToUpload(getPdf1point2gbId(), APPLICATION_PDF_VALUE,this::uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage);
    }

    private void streamBlobToUpload(String fileName,String mimeType, TriConsumer<InputStream, String, String> uploadFunction) {
        assumeNotNull(blobReader);
        Optional<BlobInfo> mayBeBlobInfo = blobReader.retrieveBlobToProcess(fileName);
        if (mayBeBlobInfo.isPresent()) {
            var blobInfo = mayBeBlobInfo.get();
            var blobClient = blobInfo.getBlobClient();
            uploadFunction.accept(blobClient.openInputStream(), fileName, mimeType);
        } else {
            fail("File not found : " + fileName);
        }
    }

    private void uploadWhitelistedLargeFileSuccessfully(InputStream inputStream, String fileName, String mimeType) {
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

    private void uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage(InputStream inputStream, String fileName, String mimeType) {
        try {
            givenRequest(getCitizen())
                    .multiPart("files", fileName, inputStream, mimeType)
                    .multiPart("classification", String.valueOf(Classifications.PUBLIC))
                    .multiPart("roles", "citizen")
                    .multiPart("roles", "caseworker")
                    .relaxedHTTPSValidation()
                    .expect().log().all()
                    .statusCode(422)
                    .body("error", equalTo("Your upload file size is more than allowed limit."))
                    .when()
                    .post("/documents");
        } catch (RuntimeException e) {
            fail(String.join(" : ",
                    "File name : ",
                    fileName, ExceptionUtils.getStackTrace(e))
            );
        }

    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}
