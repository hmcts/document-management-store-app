package uk.gov.hmcts.dm.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


@Service
public class PasswordVerifier {

    private final Logger logger = LoggerFactory.getLogger(PasswordVerifier.class);

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {
        if (!multipartFile.isEmpty()) {
            try {
                InputStream inputStream = multipartFile.getInputStream();
                ByteArrayInputStream byteArrayInputStream =
                        new ByteArrayInputStream(inputStream.readNBytes(1024 * 1024));
                logger.info("Validating multipart files against pwd after reading first 1MB");
                new AutoDetectParser().parse(byteArrayInputStream,
                        new DefaultHandler(), new Metadata(), new ParseContext());

            } catch (TikaException e) {
                logger.error("Document with Name : {} is password protected", multipartFile.getOriginalFilename());
                return false;
            } catch (IOException | SAXException e) {
                logger.info("Document with Name : {} could not be parsed", multipartFile.getOriginalFilename());
                return true;
            }
        }
        logger.info("Validating multipart files against pwd completed");
        return true;
    }
}
