package uk.gov.hmcts.dm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MultipartFileWhiteListValidator implements ConstraintValidator<MultipartFileWhiteList,MultipartFile> {

    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public void initialize(MultipartFileWhiteList fileWhiteList) {
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return fileContentVerifier.verifyContentType(multipartFile);
    }



}
