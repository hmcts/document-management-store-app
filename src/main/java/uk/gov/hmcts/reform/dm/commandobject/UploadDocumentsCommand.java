package uk.gov.hmcts.reform.dm.commandobject;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.dm.security.Classifications;
import uk.gov.hmcts.reform.dm.security.MultipartFileListWhiteList;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by pawel on 04/10/2017.
 */
@Data
public class UploadDocumentsCommand {

    @NotNull(message = "Provide collection of files to be uploaded")
    @Size(min = 1, message = "Collection of files must be bigger than 1")
    @MultipartFileListWhiteList(message = "One of the mime-types is not white-listed")
    private List<MultipartFile> files;

    @NotNull(message = "Please provide classification")
    private Classifications classification;

    private List<String> roles;

}
