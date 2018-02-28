package uk.gov.hmcts.dm.commandobject;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.MultipartFileWhiteList;

import javax.validation.constraints.NotNull;

/**
 * Created by pawel on 04/10/2017.
 */
@Data
public class UploadDocumentVersionCommand {

    @NotNull
    @MultipartFileWhiteList
    private MultipartFile file;
}
