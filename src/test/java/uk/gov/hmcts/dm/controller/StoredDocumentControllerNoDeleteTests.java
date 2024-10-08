package uk.gov.hmcts.dm.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class StoredDocumentControllerNoDeleteTests extends ComponentTestBase {

    @Autowired
    private StoredDocumentController controller;

    final UUID id = UUID.randomUUID();

    final DocumentContentVersion documentContentVersion;

    final StoredDocument storedDocument;

    public StoredDocumentControllerNoDeleteTests() {
        documentContentVersion = DocumentContentVersion.builder()
            .id(id)
            .mimeType("text/plain")
            .originalDocumentName("filename.txt")
            .storedDocument(StoredDocument.builder().id(id).build())
            .build();
        storedDocument = StoredDocument.builder().id(id)
            .documentContentVersions(
                Stream.of(documentContentVersion)
                    .toList()
            ).build();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .delete("/documents/" + id)
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testHardDelete() throws Exception {
        restActions
                .withAuthorizedUser("userId")
                .delete("/documents/" + id + "?permanent=true")
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

}
