package uk.gov.hmcts.dm.service;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class FileSizeVerifierTest {

    @InjectMocks
    FileSizeVerifier fileSizeVerifier;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaMimeTypes", Arrays.asList("audio/mpeg","video/mp4"));
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaFileSize", 500L);
        ReflectionTestUtils.setField(fileSizeVerifier, "nonMediaFileSize", 1024L);
    }

    @Test
    public void verifyFileSizeNull() {
        assertFalse(fileSizeVerifier.verifyFileSize(null));
    }

    @Test
    public void verifyFileSizeNonMediaFile() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertTrue(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    public void verifyFileSizeNonMediaFileWithZeroSize() {
        //We had to set size limit to zero as we can't have big files uploaded in to github for the tests to run.
        ReflectionTestUtils.setField(fileSizeVerifier, "nonMediaFileSize", 0L);
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertFalse(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    public void verifyFileSizeMediaFile() throws IOException {
        //We had to set size limit to zero as we can't have big files uploaded in to github for the tests to run.
        ReflectionTestUtils.setField(fileSizeVerifier, "mediaFileSize", 0L);
        MockMultipartFile kmlfile = new MockMultipartFile("data", "mp4.mp4",
            "audio/mpeg", fileToByteArray("files/audio_test.mp3"));
        assertFalse(fileSizeVerifier.verifyFileSize(kmlfile));
    }

    @Test
    public void verifyMinFileSize() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "some kml".getBytes());
        assertTrue(fileSizeVerifier.verifyMinFileSize(kmlfile));
    }

    @Test
    public void verifyLowerThanMinFileSize() {
        MockMultipartFile kmlfile = new MockMultipartFile("data", "filename.kml", "text/plain", "".getBytes());
        assertFalse(fileSizeVerifier.verifyMinFileSize(kmlfile));
    }

    private byte[] fileToByteArray(String file) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toByteArray(classLoader.getResourceAsStream(file));
    }
}
