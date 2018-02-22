package uk.gov.hmcts.dm.commandobject;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteList;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;



@Data
public class UploadDocumentsCommand {

    @NotNull(message = "Provide some files to be uploaded.")
    @Size(min = 1, message = "Please provide at least one file to be uploaded.")
    @MultipartFileListWhiteList(message = "Your upload contains a disallowed file type.")
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
    private Date ttl;

}
