package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DocumentContentVersionServiceTests {

    @Mock
    DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    StoredDocumentRepository storedDocumentRepository;

    @InjectMocks
    DocumentContentVersionService documentContentVersionService;

    @Test
    void testFindOne() {
        when(documentContentVersionRepository
            .findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(new DocumentContentVersion()));
        assertNotNull(documentContentVersionService.findById(TestUtil.RANDOM_UUID));
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository
            .findByIdAndDeleted(TestUtil.RANDOM_UUID, false))
                .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));
        assertEquals(Optional.of(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion()),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

    @Test
    void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findByIdAndDeleted(TestUtil.RANDOM_UUID, false)).thenReturn(Optional.empty());
        assertEquals(Optional.empty(),
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID));
    }

}
