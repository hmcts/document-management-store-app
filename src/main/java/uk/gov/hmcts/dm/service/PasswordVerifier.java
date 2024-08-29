package uk.gov.hmcts.dm.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import uk.gov.hmcts.dm.config.ToggleConfiguration;

import java.io.IOException;


@Service
public class PasswordVerifier {

    private final ToggleConfiguration toggleConfiguration;

    @Autowired
    public PasswordVerifier(ToggleConfiguration toggleConfiguration) {
        this.toggleConfiguration = toggleConfiguration;
    }


    private final Logger logger = LoggerFactory.getLogger(PasswordVerifier.class);

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {
        if (toggleConfiguration.isPasswordcheck()) {
            if (!multipartFile.isEmpty()) {
                try {
                    BodyContentHandler handler = new BodyContentHandler();
                    Metadata metadata = new Metadata();
                    ParseContext pcontext = new ParseContext();

                    new AutoDetectParser().parse(multipartFile.getInputStream(), handler, metadata, pcontext);

                } catch (TikaException | IOException | SAXException e) {
                    logger.info("Document with Name : {} could not be parsed", multipartFile.getOriginalFilename());
                    return false;
                }
            }
        }
        return true;
    }
}
