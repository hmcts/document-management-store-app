package uk.gov.hmcts.dm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MultipartFileListWhiteListValidator
    implements ConstraintValidator<MultipartFileListWhiteList, List<MultipartFile>> {

    private static final Logger log = LoggerFactory.getLogger(MultipartFileListWhiteListValidator.class);

    public static final String VERIFICATION_RESULTS_MAP_KEY = "uk.gov.hmcts.dm.file.verificationResultsMap";

    private final FileContentVerifier fileContentVerifier;

    @Autowired
    public MultipartFileListWhiteListValidator(FileContentVerifier fileContentVerifier) {
        this.fileContentVerifier = fileContentVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return true;
        }

        log.info("Validating {} multipart files against whitelist criteria", multipartFiles.size());

        Map<MultipartFile, FileVerificationResult> resultsMap = multipartFiles.stream()
            .collect(Collectors.toMap(
                file -> file,
                fileContentVerifier::verifyContentType
            ));

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            request.setAttribute(VERIFICATION_RESULTS_MAP_KEY, resultsMap);
        }

        boolean allValid = resultsMap.values().stream().allMatch(FileVerificationResult::valid);
        if (!allValid) {
            log.error("One or more files failed content type validation.");
        }
        return allValid;
    }
}
