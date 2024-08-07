package uk.gov.hmcts.dm.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Service
public class PasswordVerifier {

    private static final String EMPTY_STRING = "";

    private static final Logger log = LoggerFactory.getLogger(PasswordVerifier.class);

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }

        //TODO: Add in further cases (.docx, ?, need to understand what formats DM-store accepts)
        // if PDF -> use PDFBox approach (checkPdfPassword)
        // else -> research java approach to checking password protected files for other filetypes

        //Accepted filetime extensions:
        //    whitelist-ext: ${DM_MULTIPART_WHITELIST_EXT:.jpg,.jpeg,.bmp,.tif,.tiff,.png,.pdf,.txt,.doc,.dot,.docx,
        //    .dotx,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.rtf,.csv,.mp3,.m4a,.mp4}

        String extensionType = getOriginalFileNameExtension(multipartFile);
        switch (extensionType) {
            case ".pdf":
                return checkPdfPassword(multipartFile);
            case ".txt":
                return checkTxtPassword(multipartFile);
            default:
                return false;
        }
    }

    private boolean checkTxtPassword(MultipartFile multipartFile) {
        //TODO: Implement password check for .txt files if possible

        return true;
    }

    private boolean checkPdfPassword(MultipartFile multipartFile) {
        File temporaryFile = new File("src/main/resources/files/tempPdf.pdf");
        try {
            multipartFile.transferTo(temporaryFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PDDocument document = Loader.loadPDF(temporaryFile)) {
            log.info("The document {} is not password protected.", temporaryFile.getName());
            return true;
        } catch (InvalidPasswordException e) {
            log.error("The document {} is password protected.", temporaryFile.getName());
            return false;
        } catch (IOException e) {
            log.error("An error occurred while trying to load the document {}.", temporaryFile.getName());
            e.printStackTrace();
            return false;
        }
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
