package uk.gov.hmcts.dm.service;

import com.google.common.util.concurrent.SimpleTimeLimiter;
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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class PasswordVerifier {

    private final Logger logger = LoggerFactory.getLogger(PasswordVerifier.class);
    private final SimpleTimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newCachedThreadPool());

    public boolean isNotPasswordProtected(MultipartFile multipartFile) {
        if (!multipartFile.isEmpty()) {

            Callable<Boolean> task = () -> {
                try {
                    new AutoDetectParser().parse(multipartFile.getInputStream(),
                            new DefaultHandler(), new Metadata(), new ParseContext());
                    return true;
                } catch (TikaException e) {
                    logger.error("Document with Name : {} is password protected. Failed with error msg {}",
                            multipartFile.getOriginalFilename(), e.getMessage());
                    return false;
                } catch (IOException | SAXException e) {
                    logger.info("Document with Name : {} could not be parsed", multipartFile.getOriginalFilename());
                    return true;
                }
            };

            try {
                // 3 seconds timeout
                return Boolean.TRUE.equals(timeLimiter.callWithTimeout(task, 3, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException e) {
                logger.info("Document with Name : {} TimedOut while parsing", multipartFile.getOriginalFilename());
                return true; // If an exception occurs, we assume the file is not password protected
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                logger.info("Document with Name : {} Thread was interrupted", multipartFile.getOriginalFilename());
                return true; // If an exception occurs, we assume the file is not password protected
            }
        }
        return true;
    }
}
