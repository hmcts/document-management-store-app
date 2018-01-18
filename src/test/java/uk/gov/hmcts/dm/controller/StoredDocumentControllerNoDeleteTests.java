package uk.gov.hmcts.dm.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.rowset.serial.SerialBlob;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by pawel on 26/05/2017.
 */
public class StoredDocumentControllerNoDeleteTests extends ComponentTestBase {

    @Autowired
    private StoredDocumentController controller;

    final DocumentContent documentContent;

    final UUID id = UUID.randomUUID();

    final DocumentContentVersion documentContentVersion;

    final StoredDocument storedDocument;

    public StoredDocumentControllerNoDeleteTests() throws SQLException {
        documentContent = new DocumentContent(new SerialBlob("some xml".getBytes(StandardCharsets.UTF_8)));
        documentContentVersion = DocumentContentVersion.builder()
            .id(id)
            .mimeType("text/plain")
            .originalDocumentName("filename.txt")
            .storedDocument(StoredDocument.builder().id(id).folder(Folder.builder().id(id).build()).build())
            .documentContent(documentContent).build();
        storedDocument = StoredDocument.builder().id(id)
            .folder(Folder.builder().id(id).build()).documentContentVersions(
                Stream.of(documentContentVersion)
                    .collect(Collectors.toList())
            ).build();
    }

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
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @Test
    public void testHardDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .withAuthorizedService("divorce")
                .delete("/documents/" + id + "?permanent=true")
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

}
