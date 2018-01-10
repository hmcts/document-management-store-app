package uk.gov.hmcts.dm.service;

import org.junit.Before;

public class DocumentThumbnailServiceTest {

    DocumentThumbnailService documentThumbnailService;

    @Before
    public void setUp() throws Exception {
        documentThumbnailService = new DocumentThumbnailService(thumbnailCreators);
    }


}
