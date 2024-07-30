package uk.gov.hmcts.dm.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class PdfPasswordVerifier {

    private PdfPasswordVerifier() {
    }

    private static final Logger log = LoggerFactory.getLogger(PdfPasswordVerifier.class);

    public boolean checkPasswordProtectedPDF(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return false;
        }

        try (PDDocument document = Loader.loadPDF(multipartFile.getInputStream())) {
            return true;
        } catch (InvalidPasswordException e) {
            log.error("The document {} is password protected.", multipartFile.getOriginalFilename());
            return false;
        } catch (IOException e) {
            log.error("An error occurred while trying to load the document {}.", multipartFile.getOriginalFilename());
            e.printStackTrace();
            return false;
        }

//        File file = new File(filePath);
//        try (PDDocument document = Loader.loadPDF(file)) {
//            log.info("The document {} is not password protected.", file.getName());
//            return true;
//        } catch (InvalidPasswordException e) {
//            log.error("The document {} is password protected.", file.getName());
//            return false;
//        } catch (IOException e) {
//            log.error("An error occurred while trying to load the document {}.", file.getName());
//            e.printStackTrace();
//            return false;
//        }
    }
}
