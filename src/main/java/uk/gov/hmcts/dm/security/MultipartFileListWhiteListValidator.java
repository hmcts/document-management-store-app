package uk.gov.hmcts.dm.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultipartFileListWhiteListValidator implements ConstraintValidator<MultipartFileListWhiteList,List<MultipartFile>> {

    @Getter
    private final List<String> mimeTypeList;

    @Autowired
    public MultipartFileListWhiteListValidator(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList) {
        this.mimeTypeList = mimeTypeList;
    }

    @Override
    public void initialize(MultipartFileListWhiteList fileWhiteList) {}

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return  CollectionUtils.isEmpty(multipartFiles)
            || multipartFiles.stream()
                .map(MultipartFile::getContentType)
                .distinct()
                .allMatch(ft -> getMimeTypeList().contains(ft));
    }



}
