package uk.gov.hmcts.dm.security;

import lombok.Getter;
import uk.gov.hmcts.dm.exception.CouldNotDetectContentType;
import uk.gov.hmcts.dm.service.FileContentDetector;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultipartFileListWhiteListValidator implements ConstraintValidator<MultipartFileListWhiteList,List<MultipartFile>> {

    @Getter
    private final List<String> mimeTypeList;

    private FileContentDetector fileContentDetector;

    @Autowired
    public MultipartFileListWhiteListValidator(
        @Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList,
        FileContentDetector fileContentDetector
    ) {
        this.mimeTypeList = mimeTypeList;
        this.fileContentDetector = fileContentDetector;
    }

    @Override
    public void initialize(MultipartFileListWhiteList fileWhiteList) {}

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return  CollectionUtils.isEmpty(multipartFiles)
            || multipartFiles.stream()
                .allMatch(ft -> {
                    String declaredType = ft.getContentType();
                    return getMimeTypeList().contains(declaredType) && doesDeclaredTypeMatchActual(declaredType, ft);
                });
    }

    private boolean doesDeclaredTypeMatchActual(String expectedFileType, MultipartFile file) {
        try {
            return fileContentDetector.checkContentType(expectedFileType, file.getInputStream());
        } catch (IOException | CouldNotDetectContentType e) {
            return false;
        }
    }



}
