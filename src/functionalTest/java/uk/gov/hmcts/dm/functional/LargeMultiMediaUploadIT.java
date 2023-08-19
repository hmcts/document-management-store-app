package uk.gov.hmcts.dm.functional;

import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.functional.blob.BlobReader;

import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeNotNull;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.dm.functional.V1MimeTypes.VIDEO_MPEG_VALUE;

@Slf4j
public class LargeMultiMediaUploadIT extends BaseIT {

    @Autowired(required = false)
    private BlobReader blobReader;

    @Test
    public void uploadSmallMp3() {
        streamBlobToUpload("267KB_mp4_video.mp4", VIDEO_MPEG_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
    }

    @Test
    public void uploadSmallPdf() {
        streamBlobToUpload("1KB.pdf", APPLICATION_PDF_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
    }

    @Test
    public void uploadLargeMp3SuccessfullyUploaded() {
        streamBlobToUpload("272MB_mp4_video.mp4", VIDEO_MPEG_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
    }

    @Test
    public void uploadLargePdfSuccessfullyUploaded() {
        streamBlobToUpload("500MB.pdf", APPLICATION_PDF_VALUE, this::uploadWhitelistedLargeFileSuccessfully);
    }

    @Test
    public void largeMp3RejectedDueToSizeValidation() {
        streamBlobToUpload("652MB_video_mp4.mp4", VIDEO_MPEG_VALUE,
            this::uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage);
    }

    @Test
    public void largePdfRejectedDueToSizeValidation() {
        streamBlobToUpload("1.2GB.pdf", APPLICATION_PDF_VALUE,
            this::uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage);
    }

    private void streamBlobToUpload(String fileName,String mimeType,
                                    TriConsumer<InputStream, String, String> uploadFunction) {
        assumeNotNull(blobReader);
        BlockBlobClient blockBlobClient = blobReader.retrieveBlobToProcess(fileName);
        uploadFunction.accept(blockBlobClient.openInputStream(), fileName, mimeType);
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

    private void uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage(
        InputStream inputStream, String fileName, String mimeType) {
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
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}
