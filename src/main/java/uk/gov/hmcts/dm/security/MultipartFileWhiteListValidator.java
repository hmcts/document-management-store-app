package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;


public class MultipartFileWhiteListValidator implements ConstraintValidator<MultipartFileWhiteList,MultipartFile> {

    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return fileContentVerifier.verifyContentType(multipartFile);
    }



}
