package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.util.List;

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
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(fileContentVerifier::verifyContentType);
    }
}
