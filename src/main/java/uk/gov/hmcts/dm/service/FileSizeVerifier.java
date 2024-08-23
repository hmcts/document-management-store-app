package uk.gov.hmcts.dm.service;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
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

import static uk.gov.hmcts.dm.utils.StringUtils.sanitiseFileName;

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

        try (InputStream inputStream = multipartFile.getInputStream();
             TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {
            long fileSizeInBytes = tikaInputStream.getLength();

            Metadata metadata = new Metadata();
            if (multipartFile.getOriginalFilename() != null) {
                metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, multipartFile.getOriginalFilename());
                metadata.add(HttpHeaders.CONTENT_TYPE, multipartFile.getContentType());
            }
            String detected = tika.detect(tikaInputStream, metadata);
            if (mediaMimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(detected))
                    && fileSizeInBytes > mediaFileSizeInBytes) {
                log.error("Warning. The uploaded Media file size {} is more than the allowed limit of: {} MB",
                        fileSizeInBytes,
                        mediaFileSize);
                return false;
            } else if (mediaMimeTypes.stream().noneMatch(m -> m.equalsIgnoreCase(detected))
                    && fileSizeInBytes > nonMediaFileSizeInBytes) {
                log.error("Warning. The uploaded Non-Media file size {} is more than the allowed limit of : {} MB",
                    fileSizeInBytes,
                    nonMediaFileSize);
                return false;
            }
        } catch (IOException e) {
            log.error("Could not verify the file content type", e);
            return false;
        }

        return true;
    }

    public boolean verifyMinFileSize(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }
        long fileSize = multipartFile.getSize();
        if (fileSize <= 0) {
            log.error("Warning. The uploaded file : {} is empty and has size: {}",
                sanitiseFileName(multipartFile.getOriginalFilename()), fileSize);
            return false;
        }
        return true;
    }
}
