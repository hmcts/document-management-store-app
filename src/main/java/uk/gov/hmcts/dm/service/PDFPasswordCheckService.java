package uk.gov.hmcts.dm.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

class PDFPasswordCheckService {

    private PDFPasswordCheckService() {
    }

    private static final Logger log = LoggerFactory.getLogger(PDFPasswordCheckService.class);

    public static void checkPasswordProtectedPDF(String filePath) {
        File file = new File(filePath);
        try (PDDocument document = Loader.loadPDF(file)) {
            log.info("The document {} is not password protected.", file.getName());
        } catch (InvalidPasswordException e) {
            log.error("The document {} is password protected.", file.getName());
        } catch (IOException e) {
            log.error("An error occurred while trying to load the document {}.", file.getName());
            e.printStackTrace();
        }
    }
}
