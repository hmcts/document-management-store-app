package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.PasswordVerifier;

import java.util.List;

public class MultipartFilePasswordValidator
    implements ConstraintValidator<MultipartFileListPasswordCheck, List<MultipartFile>> {

    private static final Logger log = LoggerFactory.getLogger(MultipartFilePasswordValidator.class);
    private final PasswordVerifier passwordVerifier;

    @Autowired
    public MultipartFilePasswordValidator(PasswordVerifier passwordVerifier) {
        this.passwordVerifier = passwordVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        log.info("Validating multipart files against password protection criteria");
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(passwordVerifier::checkPasswordProtectedFile);
    }
}
