package uk.gov.hmcts.dm.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
import java.util.Locale;

@Service
public class FileContentVerifier {

    private final List<String> mimeTypeList;

    private final List<String> extensionsList;

    private final Tika tika = new Tika();

    private static final String EMPTY_STRING = "";

    private static final Logger log = LoggerFactory.getLogger(FileContentVerifier.class);
    private static final String PDF_MIME = "application/pdf";

    public FileContentVerifier(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList,
                               @Value("#{'${dm.multipart.whitelist-ext}'.split(',')}") List<String> extensionsList) {
        this.mimeTypeList = mimeTypeList;
        this.extensionsList = extensionsList;
    }

    public boolean verifyContentType(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }

        String fileNameExtension = getOriginalFileNameExtension(multipartFile);
        if (!extensionsList.stream().anyMatch(ext -> ext.equalsIgnoreCase(fileNameExtension))) {
            log.info(
                "Warning. The extension of uploaded file is not white-listed: fileNameExtension {}",
                fileNameExtension
            );
            return false;
        }

        try (InputStream inputStream = multipartFile.getInputStream();
             TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {
            Metadata metadata = new Metadata();
            if (multipartFile.getOriginalFilename() != null) {
                metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, multipartFile.getOriginalFilename());
                metadata.add(Metadata.CONTENT_TYPE, multipartFile.getContentType());
            }
            String detected = tika.detect(tikaInputStream, metadata);
            if (mimeTypeList.stream().noneMatch(m -> m.equalsIgnoreCase(detected))) {
                log.error("Warning. The mime-type of uploaded file is not white-listed: {}", detected);
                return false;
            }

            if (PDF_MIME.equalsIgnoreCase(detected)) {
                try {
                    PDDocument document = PDDocument.load(tikaInputStream.getFile());
                    if (document.isEncrypted()) {
                        log.error("Warning. PDF file is encrypted");
                        return false;
                    }
                } catch (InvalidPasswordException ex) {
                    log.error("Warning. PDF file is password protected");
                    return false;
                }
            }

        } catch (IOException e) {
            log.error("Could not verify the file content type", e);
            return false;
        }

        return true;
    }

    private String getOriginalFileNameExtension(MultipartFile multipartFile) {
        String originalFileName = multipartFile.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFileName)) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                return originalFileName.substring(originalFileName.lastIndexOf('.'), originalFileName.length())
                    .toLowerCase(Locale.UK);
            }
        }
        return EMPTY_STRING;
    }

}
