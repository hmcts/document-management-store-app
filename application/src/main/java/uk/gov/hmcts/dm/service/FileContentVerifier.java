package uk.gov.hmcts.dm.service;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Service
public class FileContentVerifier {

    private final List<String> mimeTypeList;

    private final List<String> extensionsList;

    private final Tika tika = new Tika();

    private static final String EMPTY_STRING = "";

    private static final Logger log = LoggerFactory.getLogger(FileContentVerifier.class);

    public FileContentVerifier(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList,
                               @Value("#{'${dm.multipart.whitelist-ext}'.split(',')}") List<String> extensionsList) {
        this.mimeTypeList = mimeTypeList;
        this.extensionsList = extensionsList;
    }

    public boolean verifyContentType(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }
        if (!mimeTypeList.stream().anyMatch(m -> m.equalsIgnoreCase(multipartFile.getContentType()))) {
            return false;
        }

        if (!extensionsList.stream().anyMatch(ext -> ext.equalsIgnoreCase(getOriginalFileNameExtension(multipartFile)))) {
            return false;
        }
        try (InputStream inputStream = multipartFile.getInputStream();
             TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {
            Metadata metadata = new Metadata();
            if (multipartFile.getOriginalFilename() != null) {
                metadata.add(Metadata.RESOURCE_NAME_KEY, multipartFile.getOriginalFilename());
                metadata.add(Metadata.CONTENT_TYPE, multipartFile.getContentType());
            }
            String detected = tika.detect(tikaInputStream, metadata);
            boolean fileTypeMatch = multipartFile.getContentType().equals(detected);
            if (!fileTypeMatch) {
                log.info(
                    String.format(
                        "Warning. Uploaded file type does not match the detected content type. Expected: %s, Detected: %s",
                        multipartFile.getContentType(), detected));
            }
            return fileTypeMatch;
        } catch (IOException e) {
            log.error("Could not verify the file content type", e);
            return false;
        }
    }

    private String getOriginalFileNameExtension(MultipartFile multipartFile) {
        String originalFileName = multipartFile.getOriginalFilename();
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return originalFileName.substring(originalFileName.lastIndexOf('.'), originalFileName.length())
                        .toLowerCase(Locale.UK);
        } else {
            return EMPTY_STRING;
        }
    }

}
