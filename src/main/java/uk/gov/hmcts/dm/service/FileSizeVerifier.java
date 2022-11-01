package uk.gov.hmcts.dm.service;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static uk.gov.hmcts.dm.utils.StringUtils.sanitiseLog;

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
            log.error("Not multi part file");
            return false;
        }

        long mediaFileSizeInBytes = mediaFileSize * 1024 * 1024;
        long nonMediaFileSizeInBytes = nonMediaFileSize * 1024 * 1024;
        String sanitisedFileName = null;
        try (InputStream inputStream = multipartFile.getInputStream();
             TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {
            sanitisedFileName = sanitiseLog(multipartFile.getOriginalFilename());
            long fileSizeInBytes = tikaInputStream.getLength();

            Metadata metadata = new Metadata();
            if (multipartFile.getOriginalFilename() != null) {
                metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, multipartFile.getOriginalFilename());
                metadata.add(Metadata.CONTENT_TYPE, multipartFile.getContentType());
            }
            String detected = tika.detect(tikaInputStream, metadata);

            if (mediaMimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(detected))
                    && fileSizeInBytes > mediaFileSizeInBytes) {
                log.error(
                    "Warning. The uploaded Media file {} size {} is more than the allowed limit of : {} MB",
                    sanitisedFileName,
                    fileSizeInBytes,
                    mediaFileSize
                );
                return false;
            } else if (mediaMimeTypes.stream().noneMatch(m -> m.equalsIgnoreCase(detected))
                    && fileSizeInBytes > nonMediaFileSizeInBytes) {
                log.error(
                    "Warning. The uploaded Non-Media file {} size {} is more than the allowed limit of : {} MB",
                    sanitisedFileName,
                    fileSizeInBytes,
                    nonMediaFileSize
                );
                return false;
            }
        } catch (IOException e) {
            log.error("Could not verify the file {} content type", sanitisedFileName, e);
            return false;
        }

        return true;
    }

}
