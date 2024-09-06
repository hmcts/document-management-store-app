package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.PasswordVerifier;

import java.util.List;

public class MultipartFilePasswordValidator
    implements ConstraintValidator<MultipartFileListPasswordCheck, List<MultipartFile>> {

    private final PasswordVerifier passwordVerifier;

    @Autowired
    public MultipartFilePasswordValidator(PasswordVerifier passwordVerifier) {
        this.passwordVerifier = passwordVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(passwordVerifier::checkPasswordProtectedFile);
    }
}
