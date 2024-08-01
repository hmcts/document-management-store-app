package uk.gov.hmcts.dm.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.PdfPasswordVerifier;

import java.util.List;

public class PdfPasswordValidator
    implements ConstraintValidator<PdfPasswordCheck, List<MultipartFile>> {

    private final PdfPasswordVerifier pdfPasswordVerifier;

    @Autowired
    public PdfPasswordValidator(PdfPasswordVerifier pdfPasswordVerifier) {
        this.pdfPasswordVerifier = pdfPasswordVerifier;
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return CollectionUtils.isEmpty(multipartFiles) || multipartFiles.stream()
            .allMatch(pdfPasswordVerifier::checkPasswordProtectedPDF);
    }
}
