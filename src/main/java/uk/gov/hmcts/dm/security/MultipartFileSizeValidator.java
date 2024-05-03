package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileSizeVerifier;

import java.util.List;

public class MultipartFileSizeValidator
    implements ConstraintValidator<MultipartFileSizeLimit,List<MultipartFile>> {

    private final FileSizeVerifier fileSizeVerifier;

    @Autowired
    public MultipartFileSizeValidator(FileSizeVerifier fileSizeVerifier) {
        this.fileSizeVerifier = fileSizeVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(fileSizeVerifier::verifyFileSize);
    }

}
