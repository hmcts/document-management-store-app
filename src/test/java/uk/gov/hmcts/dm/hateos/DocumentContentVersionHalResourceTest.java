package uk.gov.hmcts.dm.hateos;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentContentVersionHalResourceTest {

    @Test
    @SuppressWarnings("unchecked")
    void documentContentVersionHasValidLinks() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setId(UUID.randomUUID());
        documentContentVersion.setStoredDocument(storedDocument);

        DocumentContentVersionHalResource halResource = new DocumentContentVersionHalResource(documentContentVersion);

        Optional<Link> document = halResource.getLink("document");
        assertEquals(format("/documents/%s", storedDocument.getId()), document.get().toUri().toString());

        Optional<Link> self = halResource.getLink("self");
        assertEquals(format("/documents/%s/versions/%s", storedDocument.getId(),
            documentContentVersion.getId()), self.get().toUri().toString());

        Optional<Link> binary = halResource.getLink("binary");
        assertEquals(format("/documents/%s/versions/%s/binary", storedDocument.getId(),
            documentContentVersion.getId()), binary.get().toUri().toString());
    }
}
