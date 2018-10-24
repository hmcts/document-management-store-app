package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class DocumentThumbnailServiceTest {

    @Mock
    private UnsupportedThumbnailCreator unsupportedThumbnailService;

    @Mock
    private InputStream inputStream;

    private DocumentThumbnailService underTest;

    private DocumentContentVersion documentContentVersion;

    @Before
    public void setUp() {
        initMocks(this);
        underTest = new DocumentThumbnailService(new HashMap(), unsupportedThumbnailService);
        documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setMimeType("ngitb");
    }

    @Test(expected = CantCreateThumbnailException.class)
    public void nullThumbnailInputStream() {
        when(unsupportedThumbnailService.getThumbnail(documentContentVersion)).thenReturn(null);
        underTest.generateThumbnail(documentContentVersion);
    }

    @Test
    public void withThumbnailInputStream() throws IOException {
        when(unsupportedThumbnailService.getThumbnail(documentContentVersion)).thenReturn(inputStream);
        final Resource resource = underTest.generateThumbnail(documentContentVersion);
        assertThat(resource.getInputStream(), is(inputStream));
    }
}
