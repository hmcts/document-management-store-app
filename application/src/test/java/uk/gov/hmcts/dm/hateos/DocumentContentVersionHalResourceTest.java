package uk.gov.hmcts.dm.hateos;

import org.junit.Test;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class DocumentContentVersionHalResourceTest {

    @Test
    public void documentContentVersionHasValidLinks() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setId(UUID.randomUUID());
        documentContentVersion.setStoredDocument(storedDocument);

        DocumentContentVersionHalResource halResource = new DocumentContentVersionHalResource(documentContentVersion);

        assertEquals(format("/documents/%s", storedDocument.getId()), halResource.getLink("document").getHref());

        assertEquals(format("/documents/%s/versions/%s", storedDocument.getId(),
            documentContentVersion.getId()), halResource.getLink("self").getHref());

        assertEquals(format("/documents/%s/versions/%s/binary", storedDocument.getId(),
            documentContentVersion.getId()), halResource.getLink("binary").getHref());

        assertEquals(format("/documents/%s/versions/%s/thumbnail", storedDocument.getId(),
            documentContentVersion.getId()), halResource.getLink("thumbnail").getHref());

        assertEquals(format("/documents/%s/versions/%s/migrate", storedDocument.getId(),
            documentContentVersion.getId()), halResource.getLink("migrate").getHref());
    }
}
