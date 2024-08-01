package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteList;
import uk.gov.hmcts.dm.security.MultipartFilePasswordCheck;
import uk.gov.hmcts.dm.security.MultipartFileSizeLimit;
import uk.gov.hmcts.dm.security.MultipartFileSizeMinimum;

import java.util.Date;
import java.util.List;
import java.util.Map;



@Data
public class UploadDocumentsCommand {

    public static final String DISALLOWED_FILE_ERR_MSG = "Your upload contains a disallowed file type.";

    public static final String FILE_SIZE_ERR_MSG = "Your upload file size is more than allowed limit.";

    public static final String FILE_SIZE_MIN_ERR_MSG = "Your upload file size is less than allowed limit.";

    public static final String PDF_PASSWORD_ERR_MSG = "Your upload file is password protected.";


    @NotNull(message = "Provide some files to be uploaded.")
    @Size(min = 1, message = "Please provide at least one file to be uploaded.")
    @MultipartFileListWhiteList(message = DISALLOWED_FILE_ERR_MSG)
    @MultipartFileSizeLimit(message = FILE_SIZE_ERR_MSG)
    @MultipartFileSizeMinimum(message = FILE_SIZE_MIN_ERR_MSG)
    @MultipartFilePasswordCheck(message = PDF_PASSWORD_ERR_MSG)
    private List<MultipartFile> files;

    @NotNull(message = "Please provide classification")
    private Classifications classification;

    @Getter
    @Setter
    private List<String> roles;

    @Getter
    @Setter
    private Map<String, String> metadata;

    @Getter
    @Setter
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date ttl;

}
