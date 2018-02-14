package uk.gov.hmcts.dm.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import uk.gov.hmcts.dm.exception.CouldNotDetectContentType;

@Service
public class FileContentDetector {

    Tika tika = new Tika();

    public boolean checkContentType(@NonNull String expectedMimeType, @NonNull InputStream input) throws CouldNotDetectContentType {
        
        try {
            String detected = tika.detect(input);
            return expectedMimeType.equals(detected);
            
        } catch (IOException e) {
            throw new CouldNotDetectContentType(expectedMimeType, e);
        }
    }

}