package uk.gov.hmcts.dm.endtoend;

import org.apache.commons.io.IOUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static uk.gov.hmcts.dm.endtoend.Helper.getThumbnailUrlFromResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:application-local.yaml")
public class ThumbnailTest {

    @Autowired
    private MockMvc mvc;

    private final HttpHeaders headers = Helper.getHeaders();

    private final MockMultipartFile txtFile;
    private final MockMultipartFile jpgFile;
    private final MockMultipartFile pngFile;
    private final MockMultipartFile gifFile;
    private final MockMultipartFile gifAniFile;
    private final MockMultipartFile pdfFile;

    public ThumbnailTest() throws IOException {
        txtFile = createMockMultipartFile("test.txt","test".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN_VALUE);
        jpgFile = createMockMultipartFile("document-jpg-example.jpg",MediaType.IMAGE_JPEG_VALUE);
        pngFile = createMockMultipartFile("document-png-example.png",MediaType.IMAGE_PNG_VALUE);
        gifFile = createMockMultipartFile("document-gif-example.gif",MediaType.IMAGE_GIF_VALUE);
        gifAniFile = createMockMultipartFile("document-gif-animated-example.gif",MediaType.IMAGE_GIF_VALUE);
        pdfFile = createMockMultipartFile("1MB.pdf",MediaType.APPLICATION_PDF_VALUE);
    }

    @Test
    public void should_upload_a_txt_and_retrieve_a_unsupported_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(txtFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void should_upload_a_pdf_and_retrieve_a_supported_pdf_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(pdfFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void should_upload_a_jpg_and_retrieve_a_supported_image_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(jpgFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void should_upload_a_png_and_retrieve_a_supported_image_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(pngFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void should_upload_a_gif_and_retrieve_a_supported_image_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(gifFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void should_upload_a_gif_ani_and_retrieve_a_supported_image_thumbnail() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(gifAniFile)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getThumbnailUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    private byte[] fileToByteArray(String file) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(file).getFile());
        return IOUtils.toByteArray(Files.newInputStream(f.toPath()));
    }

    private MockMultipartFile createMockMultipartFile(String file,String mimeType) throws IOException {
        byte[] f = fileToByteArray("files/" + file);
        return new MockMultipartFile("files", file, mimeType, f);
    }

    private MockMultipartFile createMockMultipartFile(String file, byte[] f, String mimeType) {
        return new MockMultipartFile("files", file,mimeType, f);
    }
}
