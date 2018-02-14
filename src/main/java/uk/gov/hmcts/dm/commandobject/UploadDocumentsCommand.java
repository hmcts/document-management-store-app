package uk.gov.hmcts.dm.commandobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;



@Data
public class UploadDocumentsCommand {

    @NotNull(message = "Provide collection of files to be uploaded")
    @Size(min = 1, message = "Collection of files must be bigger than 1")
    @MultipartFileListWhiteList(message = "One of the mime-types is not white-listed")
    @JsonIgnore
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
