package uk.gov.hmcts.reform.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.dm.componenttests.TestUtil;
import uk.gov.hmcts.reform.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.reform.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.reform.dm.domain.DocumentContent;
import uk.gov.hmcts.reform.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.reform.dm.repository.DocumentContentVersionRepository;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pawel on 11/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentContentVersionServiceTests {

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    private DocumentContentVersionService documentContentVersionService;

    @Test
    public void testStreamingOfFileContentVersion() throws Exception {

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        documentContentVersionService.setStreamBufferSize(10000);

        documentContentVersionService.setResponse(mockHttpServletResponse);

        documentContentVersionService.streamDocumentContentVersion(TestUtil.DOCUMENT_CONTENT_VERSION);

        Assert.assertEquals(
                mockHttpServletResponse.getHeader(HttpHeaders.CONTENT_TYPE),
                TestUtil.DOCUMENT_CONTENT_VERSION.getMimeType());

        Assert.assertEquals(
                mockHttpServletResponse.getHeader(HttpHeaders.CONTENT_LENGTH),
                TestUtil.DOCUMENT_CONTENT_VERSION.getSize().toString());

        Assert.assertEquals(mockHttpServletResponse.getContentAsString(),
                TestUtil.BLOB_DATA);

        Assert.assertTrue(documentContentVersionService.getStreamBufferSize() > 0);

    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentNull() throws Exception {
        documentContentVersionService.streamDocumentContentVersion(new DocumentContentVersion());
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentDataNull() throws Exception {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setDocumentContent(new DocumentContent());
        documentContentVersionService.streamDocumentContentVersion(documentContentVersion);
    }

    @Test(expected = NullPointerException.class)
    public void testStreamingOfFileContentVersionNull() throws Exception {
        documentContentVersionService.streamDocumentContentVersion(null);
    }


    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionWithException() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("err"));
        documentContentVersionService.setResponse(response);
        documentContentVersionService.streamDocumentContentVersion(TestUtil.DOCUMENT_CONTENT_VERSION);
    }

    @Test
    public void testFindOne() throws Exception {
        when(documentContentVersionRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(new DocumentContentVersion());
        Assert.assertNotNull(documentContentVersionService.findOne(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileId() throws Exception {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(TestUtil.STORED_DOCUMENT);
        Assert.assertEquals(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion(), documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() throws Exception {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(null);
        Assert.assertNull(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

}
