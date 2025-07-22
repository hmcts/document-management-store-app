package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileSizeVerifier;

import java.util.List;

public class MultipartFileSizeMinValidator
    implements ConstraintValidator<MultipartFileSizeMinimum, List<MultipartFile>> {
    private static final Logger log = LoggerFactory.getLogger(MultipartFileSizeMinValidator.class);
    private final FileSizeVerifier fileSizeVerifier;

    @Autowired
    public MultipartFileSizeMinValidator(FileSizeVerifier fileSizeVerifier) {
        this.fileSizeVerifier = fileSizeVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        log.info("Validating multipart files against minimum size criteria");
        return CollectionUtils.isEmpty(multipartFiles)
            || multipartFiles.stream().allMatch(fileSizeVerifier::verifyMinFileSize);
    }
}
