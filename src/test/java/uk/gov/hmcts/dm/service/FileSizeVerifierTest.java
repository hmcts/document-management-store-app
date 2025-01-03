package uk.gov.hmcts.dm.service;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
class FileSizeVerifierTest {

    @InjectMocks
    private FileSizeVerifier fileSizeVerifier;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaMimeTypes", Arrays.asList("audio/mpeg","video/mp4"));
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaFileSize", 500L);
        ReflectionTestUtils.setField(fileSizeVerifier, "nonMediaFileSize", 1024L);
    }

    @Test
    void verifyFileSizeNull() {
        assertFalse(fileSizeVerifier.verifyFileSize(null));
    }

    @Test
    void verifyFileSizeNonMediaFile() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertTrue(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    void verifyFileSizeNonMediaFileWithZeroSize() {
        //We had to set size limit to zero as we can't have big files uploaded in to github for the tests to run.
        ReflectionTestUtils.setField(fileSizeVerifier, "nonMediaFileSize", 0L);
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertFalse(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    void verifyFileSizeMediaFile() throws IOException {
        //We had to set size limit to zero as we can't have big files uploaded in to github for the tests to run.
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaFileSize", 0L);
        MockMultipartFile kmlfile = new MockMultipartFile("data", "mp4.mp4",
            "audio/mpeg", fileToByteArray("files/audio_test.mp3"));
        assertFalse(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    void verifyMinFileSize() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertTrue(fileSizeVerifier.verifyMinFileSize(kmlfile));
    }

    @Test
    void verifyLowerThanMinFileSize() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "".getBytes());
        assertFalse(fileSizeVerifier.verifyMinFileSize(kmlfile));
    }

    private byte[] fileToByteArray(String file) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toByteArray(classLoader.getResourceAsStream(file));
    }
}
