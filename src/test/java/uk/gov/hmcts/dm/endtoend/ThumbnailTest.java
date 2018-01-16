package uk.gov.hmcts.dm.endtoend;


import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.security.Classifications;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static uk.gov.hmcts.dm.endtoend.Helper.getBinaryUrlFromResponse;
import static uk.gov.hmcts.dm.endtoend.Helper.getThumbnailUrlFromResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(
    locations = "classpath:application-local.yaml")
public class ThumbnailTest {


    @Autowired
    private MockMvc mvc;
    private HttpHeaders headers = Helper.getHeaders();

    private MockMultipartFile TXT_FILE;
    private MockMultipartFile JPG_FILE;
    private MockMultipartFile PNG_FILE;
    private MockMultipartFile GIF_FILE;
    private MockMultipartFile GIF_ANI_FILE;
    private MockMultipartFile PDF_FILE;

    @Before
    public void setUp() throws Exception {
        PDF_FILE = createMockMultipartFile("1MB.pdf",MediaType.APPLICATION_PDF);
        GIF_FILE = createMockMultipartFile("document-gif-example.gif",MediaType.IMAGE_GIF);
        GIF_ANI_FILE = createMockMultipartFile("document-gif-animated-example.gif",MediaType.IMAGE_GIF);
        JPG_FILE = createMockMultipartFile("document-jpg-example.jpg",MediaType.IMAGE_JPEG);
        PNG_FILE = createMockMultipartFile("document-png-example.png",MediaType.IMAGE_PNG);
        TXT_FILE = createMockMultipartFile("test.txt","test".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
    }

    @Test
    public void should_upload_a_txt_and_retrieve_a_unsupported_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(TXT_FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().bytes(JPG_FILE.getBytes()));
    }

    @Test
    public void should_upload_a_pdf_and_retrieve_a_supported_pdf_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(PDF_FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().bytes(JPG_FILE.getBytes()));
    }

    @Test
    public void should_upload_a_jpg_and_retrieve_a_supported_image_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(JPG_FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().bytes(JPG_FILE.getBytes()));
    }



    private byte[] fileToByteArray(String file) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(file).getFile());
        return IOUtils.toByteArray(Files.newInputStream(f.toPath()));
    }

    private MockMultipartFile createMockMultipartFile(String file,MediaType mimeType) throws IOException {
        byte[] f = fileToByteArray("files/"+file);
        return new MockMultipartFile("files", file, mimeType.getType(), f);
    }
    private MockMultipartFile createMockMultipartFile(String file, byte[] f, MediaType mimeType) {
        return new MockMultipartFile("files", file,mimeType.getType(), f);
    }

}
