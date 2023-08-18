package uk.gov.hmcts.dm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultipartFileListWhiteListValidator
    implements ConstraintValidator<MultipartFileListWhiteList,List<MultipartFile>> {

    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileListWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public void initialize(MultipartFileListWhiteList fileWhiteList) {
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream().allMatch(ft ->
            fileContentVerifier.verifyContentType(ft));
    }

}
