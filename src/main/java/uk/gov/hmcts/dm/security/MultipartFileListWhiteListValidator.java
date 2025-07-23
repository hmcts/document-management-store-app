package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.util.List;

public class MultipartFileListWhiteListValidator
    implements ConstraintValidator<MultipartFileListWhiteList,List<MultipartFile>> {

    private static final Logger log = LoggerFactory.getLogger(MultipartFileListWhiteListValidator.class);
    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileListWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        log.info("Validating multipart files against whitelist criteria");
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(fileContentVerifier::verifyContentType);
    }
}
