package uk.gov.hmcts.dm.functional;

import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.functional.blob.BlobReader;

import java.io.InputStream;
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.dm.functional.V1MimeTypes.VIDEO_MPEG_VALUE;

@Slf4j
@SuppressWarnings("java:S6813") // Suppress SonarQube warning for autowired field
public class LargeMultiMediaUploadIT extends BaseIT {

    @Autowired(required = false)
    private BlobReader blobReader;
    private static final String ROLES_CONST = "roles";
    private static final String CITIZEN_CONST = "citizen";
    private static final String CASEWORKER_CONST = "caseworker";

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
        assumeTrue(Objects.nonNull(blobReader));
        BlockBlobClient blockBlobClient = blobReader.retrieveBlobToProcess(fileName);
        uploadFunction.accept(blockBlobClient.openInputStream(), fileName, mimeType);
    }

    private void uploadWhitelistedLargeFileSuccessfully(InputStream inputStream, String fileName, String mimeType) {
        givenRequest(getCitizen())
            .multiPart("files", fileName, inputStream, mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)

            .body("_embedded.documents[0].originalDocumentName", equalTo(fileName))
            .body("_embedded.documents[0].mimeType", equalTo(mimeType))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0].roles[0]", equalTo(CASEWORKER_CONST))
            .body("_embedded.documents[0].roles[1]", equalTo(CITIZEN_CONST))
            .when()
            .post("/documents");
    }

    private void uploadingLargeFileBeyoundLimitThrowsValidationSizeErrorMessage(
        InputStream inputStream, String fileName, String mimeType) {
        givenRequest(getCitizen())
            .multiPart("files", fileName, inputStream, mimeType)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .multiPart(ROLES_CONST, CASEWORKER_CONST)
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
