package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by pawel on 11/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentContentVersionServiceTests {

    @Mock
    DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @Mock
    AzureFileStorageService azureFileStorageService;

    @InjectMocks
    DocumentContentVersionService documentContentVersionService;

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

        Assert.assertEquals(
                mockHttpServletResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION),
            String.format(
                "fileName=\"%s\"",
                TestUtil.DOCUMENT_CONTENT_VERSION.getOriginalDocumentName()));

        verify(azureFileStorageService, times(1))
            .streamBinary(TestUtil.DOCUMENT_CONTENT_VERSION, mockHttpServletResponse.getOutputStream());



    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentNull() {
        documentContentVersionService.streamDocumentContentVersion(new DocumentContentVersion());
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentDataNull() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersionService.streamDocumentContentVersion(documentContentVersion);
    }

    @Test(expected = NullPointerException.class)
    public void testStreamingOfFileContentVersionNull() {
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
    public void testFindOne() {
        when(documentContentVersionRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(new DocumentContentVersion());
        Assert.assertNotNull(documentContentVersionService.findOne(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(TestUtil.STORED_DOCUMENT);
        Assert.assertEquals(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion(), documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(null);
        Assert.assertNull(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

}
