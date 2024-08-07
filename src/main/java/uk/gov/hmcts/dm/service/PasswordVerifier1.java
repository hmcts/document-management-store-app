package uk.gov.hmcts.dm.service;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Service
public class PasswordVerifier1 {

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) throws IOException {

        InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
        String documentType = detectDocTypeUsingFacade(inputStream);
        BodyContentHandler handler = new BodyContentHandler();

        switch (documentType) {
            case "application/pdf":
                return checkPdfPassword(inputStream);
            case "text/plain":
                return true;
            default:
                return false;
        }
    }

    private boolean checkPdfPassword(InputStream inputStream) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext pcontext = new ParseContext();

            PDFParser pdfparser = new PDFParser();
            pdfparser.parse(inputStream, handler, metadata,pcontext);
        } catch (TikaException | IOException e) {
                return false;
            }
        return true;
    }

    private static String detectDocTypeUsingFacade(InputStream stream)
        throws IOException {

        Tika tika = new Tika();
        return tika.detect(stream);
    }
}
