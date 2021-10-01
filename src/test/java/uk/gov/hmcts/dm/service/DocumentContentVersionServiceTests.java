package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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

        Assert.assertEquals(mockHttpServletResponse.getContentAsString(),
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
        when(documentContentVersionRepository.findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(new DocumentContentVersion()));
        Assert.assertNotNull(documentContentVersionService.findById(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository
            .findByIdAndDeleted(TestUtil.RANDOM_UUID, false))
                .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));
        Assert.assertEquals(Optional.of(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion()),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findByIdAndDeleted(TestUtil.RANDOM_UUID, false)).thenReturn(Optional.empty());
        Assert.assertEquals(Optional.empty(),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

}
