package uk.gov.hmcts.dm.service;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileSizeVerifier {

    @Value("#{'${dm.mediafile.whitelist}'.split(',')}")
    private List<String> mediaMimeTypes;

    @Value("${dm.mediafile.sizelimit}")
    private Long mediaFileSize;

    @Value("${dm.nonmediafile.sizelimit}")
    private Long nonMediaFileSize;

    private final Tika tika = new Tika();

    private static final Logger log = LoggerFactory.getLogger(FileSizeVerifier.class);

    public boolean verifyFileSize(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }

        long mediaFileSizeInBytes = mediaFileSize * 1024 * 1024;
        long nonMediaFileSizeInBytes = nonMediaFileSize * 1024 * 1024;

        long fileSizeInBytes = multipartFile.getSize();

        if (mediaMimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(multipartFile.getContentType()))
                && fileSizeInBytes  > mediaFileSizeInBytes) {
            log.error(
                String.format("Warning. The uploaded Media file size %s is more than the allowed limit of : %s MB", fileSizeInBytes, mediaFileSize));
            return false;
        } else if (mediaMimeTypes.stream().noneMatch(m -> m.equalsIgnoreCase(multipartFile.getContentType()))
                && fileSizeInBytes > nonMediaFileSizeInBytes) {
            log.error(
                String.format("Warning. The uploaded Non-Media file size %s is more than the allowed limit of : %s MB", fileSizeInBytes, nonMediaFileSize));
            return false;
        }

        return true;
    }

}
