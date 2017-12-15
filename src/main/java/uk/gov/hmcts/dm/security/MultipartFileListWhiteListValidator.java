package uk.gov.hmcts.dm.security;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class MultipartFileListWhiteListValidator implements ConstraintValidator<MultipartFileListWhiteList,List<MultipartFile>> {

    private static final Logger log = LoggerFactory.getLogger(MultipartFileListWhiteListValidator.class);


    @Getter
    private final List<String> mimeTypeList;

    @Autowired
    public MultipartFileListWhiteListValidator(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList){
        this.mimeTypeList = mimeTypeList;
    }

    @Override
    public void initialize(MultipartFileListWhiteList fileWhiteList) {
        String msg = fileWhiteList.message();
        log.info("MultipartFileListWhiteListValidator initialize ", msg);
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        return  CollectionUtils.isEmpty(multipartFiles) ||
                multipartFiles.stream()
                .map(MultipartFile::getContentType)
                .distinct()
                .allMatch(ft -> getMimeTypeList().contains(ft));
    }

}
