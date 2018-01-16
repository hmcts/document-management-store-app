package uk.gov.hmcts.dm.controller;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by pawel on 26/05/2017.
 */
public class StoredDocumentControllerNoDeleteTests extends ComponentTestBase {

    final DocumentContent documentContent = new DocumentContent(new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8)));

    final UUID id = UUID.randomUUID();

    final DocumentContentVersion documentContentVersion = DocumentContentVersion.builder()
            .id(id)
            .mimeType("text/plain")
            .originalDocumentName("filename.txt")
            .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
            .documentContent(documentContent).build();

    final StoredDocument storedDocument = StoredDocument.builder().id(id)
                        .folder(Folder.builder().id(id).build()).documentContentVersions(
                        Stream.of(documentContentVersion)
                                .collect(Collectors.toList())
                ).build();

    public StoredDocumentControllerNoDeleteTests() throws Exception {
    }

    @Autowired
    private StoredDocumentController controller;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        controller.setDeleteEnabled(false);
    }

    @Test
    public void testDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id)
                .andExpect(status().is(HttpStatus.NOT_IMPLEMENTED.value()));
    }

    @Test
    public void testHardDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id + "/removePermanently")
                .andExpect(status().is(HttpStatus.NOT_IMPLEMENTED.value()));
    }


}
