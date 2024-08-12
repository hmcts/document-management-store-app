package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.PasswordVerifier;
import uk.gov.hmcts.dm.service.PasswordVerifier1;

import java.util.List;

public class MultipartFilePasswordValidator
    implements ConstraintValidator<MultipartFilePasswordCheck, List<MultipartFile>> {

    private final PasswordVerifier1 passwordVerifier;

    @Autowired
    public MultipartFilePasswordValidator(PasswordVerifier1 passwordVerifier) {
        this.passwordVerifier = passwordVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(passwordVerifier::checkPasswordProtectedFile);
    }
}
