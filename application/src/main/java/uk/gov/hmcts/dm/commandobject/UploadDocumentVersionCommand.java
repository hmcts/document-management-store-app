package uk.gov.hmcts.dm.commandobject;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import uk.gov.hmcts.dm.security.MultipartFileWhiteList;

@Data
public class UploadDocumentVersionCommand {

    @NotNull
    @MultipartFileWhiteList
    private MultipartFile file;
}
