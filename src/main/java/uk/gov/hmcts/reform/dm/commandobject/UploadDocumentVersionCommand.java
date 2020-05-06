package uk.gov.hmcts.reform.dm.commandobject;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.dm.security.MultipartFileWhiteList;

import javax.validation.constraints.NotNull;

@Data
public class UploadDocumentVersionCommand {

    @NotNull
    @MultipartFileWhiteList
    private MultipartFile file;
}
