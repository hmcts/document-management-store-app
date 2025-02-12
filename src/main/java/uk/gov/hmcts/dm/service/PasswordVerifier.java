package uk.gov.hmcts.dm.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;


@Service
public class PasswordVerifier {

    private final Logger logger = LoggerFactory.getLogger(PasswordVerifier.class);

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {
        if (!multipartFile.isEmpty()) {
            try {
                new AutoDetectParser().parse(multipartFile.getInputStream(), new BodyContentHandler(),
                    new Metadata(), new ParseContext());

            } catch (TikaException e) {
                logger.error("Document with Name : {} is password protected", multipartFile.getOriginalFilename());
                return false;
            } catch (IOException | SAXException e) {
                logger.info("Document with Name : {} could not be parsed", multipartFile.getOriginalFilename());
                return true;
            }
        }
        return true;
    }
}
