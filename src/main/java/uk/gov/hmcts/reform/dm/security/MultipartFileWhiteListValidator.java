package uk.gov.hmcts.reform.dm.security;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultipartFileWhiteListValidator implements ConstraintValidator<MultipartFileWhiteList, MultipartFile> {

    private static final Logger log = LoggerFactory.getLogger(MultipartFileWhiteListValidator.class);

    @Getter
    private final List<String> mimeTypeList;

    @Autowired
    public MultipartFileWhiteListValidator(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList) {
        this.mimeTypeList = mimeTypeList;
    }

    @Override
    public void initialize(MultipartFileWhiteList fileWhiteList) {
        String msg = fileWhiteList.message();
        log.info("MultipartFileListWhiteListValidator initialize ", msg);
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return multipartFile == null || getMimeTypeList().contains(multipartFile.getContentType());
    }

}
