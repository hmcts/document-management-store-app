package uk.gov.hmcts.dm.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class PasswordVerifier {

    private static final Logger log = LoggerFactory.getLogger(PasswordVerifier.class);

    public boolean checkPasswordProtectedPDF(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }

        //TODO: Add in filetype verifier logic before the password check, similar to FileContentVerifier

//        private String getOriginalFileNameExtension(MultipartFile multipartFile) {
//            String originalFileName = multipartFile.getOriginalFilename();
//            if (StringUtils.isNotBlank(originalFileName)) {
//                int lastDotIndex = originalFileName.lastIndexOf('.');
//                if (lastDotIndex >= 0) {
//                    return originalFileName.substring(originalFileName.lastIndexOf('.'), originalFileName.length())
//                        .toLowerCase(Locale.UK);
//                }
//            }
//            return EMPTY_STRING;
//        }

        // if PDF -> use PDFBox approach below
        // else -> research java approach to checking password protected files for other filetypes

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
}
