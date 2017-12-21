package uk.gov.hmcts.reform.dm.componenttests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.reform.dm.domain.DocumentContent;
import uk.gov.hmcts.reform.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.reform.dm.domain.Folder;
import uk.gov.hmcts.reform.dm.domain.StoredDocument;
import uk.gov.hmcts.reform.dm.exception.DmRuntimeException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.rowset.serial.SerialBlob;

public class TestUtil {

    public static final UUID RANDOM_UUID = UUID.randomUUID();


    public static final String BLOB_DATA = "data";

    public static final DocumentContent DOCUMENT_CONTENT;

    static {
        try {
            DOCUMENT_CONTENT = new DocumentContent(new SerialBlob(BLOB_DATA.getBytes(StandardCharsets.UTF_8)));
        } catch (SQLException e) {
            throw new DmRuntimeException(e);
        }
    }

    public static final MockMultipartFile TEST_FILE;

    public static final MockMultipartFile TEST_FILE_WITH_FUNNY_NAME;

    static {
        try {
            TEST_FILE = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes(StandardCharsets.UTF_8));
            TEST_FILE_WITH_FUNNY_NAME = new MockMultipartFile("file", "filename!@£$%^&*()<>.txt", "text/plain", "some xml".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new DmRuntimeException(e);
        }
    }

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    public static final MediaType MULTIPART_FORM_DATA = new MediaType(MediaType.MULTIPART_FORM_DATA.getType(), MediaType.MULTIPART_FORM_DATA.getSubtype());


    public static final Folder TEST_FOLDER =
        new Folder(RANDOM_UUID, "name", null, null, null, null, null);


    public static final DocumentContentVersion DOCUMENT_CONTENT_VERSION = DocumentContentVersion.builder()
        .id(RANDOM_UUID)
        .mimeType("text/plain")
        .originalDocumentName("filename.txt")
        .size(4L)
        .storedDocument(
            StoredDocument.builder()
                .id(RANDOM_UUID)
                .folder(Folder.builder().id(RANDOM_UUID).build())
                .build())
        .documentContent(DOCUMENT_CONTENT).build();

    public static final Folder folder = Folder.builder()
        .id(RANDOM_UUID)
        .storedDocuments(
            Stream.of(StoredDocument.builder().id(RANDOM_UUID).documentContentVersions(
                Stream.of(DOCUMENT_CONTENT_VERSION).collect(Collectors.toList())
            ).build())
                .collect(Collectors.toList())
        )
        .build();

    public static final StoredDocument STORED_DOCUMENT = StoredDocument.builder().id(RANDOM_UUID)
        .folder(Folder.builder().id(RANDOM_UUID).build()).documentContentVersions(
            Stream.of(DOCUMENT_CONTENT_VERSION)
                .collect(Collectors.toList())
        ).build();

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper om = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return om.writeValueAsBytes(object);
    }

    public static String convertObjectToJsonString(Object object) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }


}
