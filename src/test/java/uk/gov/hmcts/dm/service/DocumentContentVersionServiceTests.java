package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DocumentContentVersionServiceTests {

    @Mock
    DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    DocumentContentVersionService documentContentVersionService;

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
