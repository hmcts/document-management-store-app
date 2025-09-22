package uk.gov.hmcts.dm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import java.util.Objects;

public class MultipartFileWhiteListValidator implements ConstraintValidator<MultipartFileWhiteList, MultipartFile> {

    public static final String VERIFICATION_RESULT_KEY = "uk.gov.hmcts.dm.file.verificationResult";

    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        FileVerificationResult result = fileContentVerifier.verifyContentType(multipartFile);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            request.setAttribute(VERIFICATION_RESULT_KEY, result);
        }

        return result.valid();
    }
}
