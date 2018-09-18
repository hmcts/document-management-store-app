package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
    private OutputStream outputStream;

    @InjectMocks
    DocumentContentVersionService documentContentVersionService;

    @Test
    public void testStreamingOfFileContentVersion() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        documentContentVersionService.streamDocumentContentVersion(TestUtil.DOCUMENT_CONTENT_VERSION,
            mockHttpServletResponse.getOutputStream());

        assertEquals(mockHttpServletResponse.getContentAsString(),
            TestUtil.BLOB_DATA);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentNull() {
        documentContentVersionService.streamDocumentContentVersion(new DocumentContentVersion(), outputStream);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionContentDataNull() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setDocumentContent(new DocumentContent());
        documentContentVersionService.streamDocumentContentVersion(documentContentVersion, outputStream);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void testStreamingOfFileContentVersionWithException() throws Exception {
        doThrow(new IOException("err")).when(outputStream).write(any(byte[].class), anyInt(), anyInt());

        documentContentVersionService.streamDocumentContentVersion(TestUtil.DOCUMENT_CONTENT_VERSION, outputStream);
    }

    @Test(expected = NullPointerException.class)
    public void testStreamingOfFileContentVersionNull() {
        documentContentVersionService.streamDocumentContentVersion(null, outputStream);
    }

    @Test
    public void testFindOne() {
        when(documentContentVersionRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(new DocumentContentVersion());
        Assert.assertNotNull(documentContentVersionService.findOne(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(TestUtil.STORED_DOCUMENT);
        assertEquals(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion(),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findOne(TestUtil.RANDOM_UUID)).thenReturn(null);
        Assert.assertNull(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

}
