package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.MultipartFileWhiteList;


@Data
public class UploadDocumentVersionCommand {

    @NotNull
    @MultipartFileWhiteList
    private MultipartFile file;
}
