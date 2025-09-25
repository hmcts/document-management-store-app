package uk.gov.hmcts.dm.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
import java.util.Locale;
import java.util.Objects;

import static uk.gov.hmcts.dm.utils.StringUtils.sanitiseFileName;

@Service
@SuppressWarnings({"squid:S2629"})
public class FileContentVerifier {

    private final List<String> mimeTypeList;

    private final List<String> extensionsList;

    private final Tika tika = new Tika();

    private static final String EMPTY_STRING = "";

    private static final String PROTECTED = "protected";

    private static final Logger log = LoggerFactory.getLogger(FileContentVerifier.class);

    public FileContentVerifier(@Value("#{'${dm.multipart.whitelist}'.split(',')}") List<String> mimeTypeList,
                               @Value("#{'${dm.multipart.whitelist-ext}'.split(',')}") List<String> extensionsList) {
        this.mimeTypeList = mimeTypeList;
        this.extensionsList = extensionsList;
    }

    public FileVerificationResult verifyContentType(MultipartFile multipartFile) {
        if (multipartFile == null) {
            log.error("Warning. MultipartFile is null. VerifyContentType failed");
            return FileVerificationResult.invalid();
        }

        String fileNameExtension = getOriginalFileNameExtension(multipartFile);
        if (extensionsList.stream().noneMatch(ext -> ext.equalsIgnoreCase(fileNameExtension))) {
            log.error("The extension {} of uploaded file with name : {} is not white-listed",
                sanitiseFileName(fileNameExtension), sanitiseFileName(multipartFile.getOriginalFilename()));
            return FileVerificationResult.invalid();
        }

        String detectedMimeType;
        try (InputStream inputStream = multipartFile.getInputStream();
             TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {

            Metadata metadata = new Metadata();
            if (Objects.nonNull(multipartFile.getOriginalFilename())) {
                metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, multipartFile.getOriginalFilename());
                metadata.add(HttpHeaders.CONTENT_TYPE, multipartFile.getContentType());
            }
            detectedMimeType = tika.detect(tikaInputStream, metadata);

            String finalDetectedMimeType = detectedMimeType;
            if (!Strings.CI.endsWith(detectedMimeType, PROTECTED)
                && mimeTypeList.stream().noneMatch(m -> m.equalsIgnoreCase(finalDetectedMimeType))) {
                log.error("The mime-type {} of uploaded file with name : {} is not white-listed: ",
                    detectedMimeType, sanitiseFileName(multipartFile.getOriginalFilename()));
                return new FileVerificationResult(false, detectedMimeType);
            }
        } catch (IOException e) {
            log.error("Could not verify the file content type", e);
            return FileVerificationResult.invalid();
        }

        return new FileVerificationResult(true, detectedMimeType);
    }

    private String getOriginalFileNameExtension(MultipartFile multipartFile) {
        String originalFileName = multipartFile.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFileName)) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                return originalFileName.substring(originalFileName.lastIndexOf('.'))
                    .toLowerCase(Locale.UK);
            }
        }
        return EMPTY_STRING;
    }
}
