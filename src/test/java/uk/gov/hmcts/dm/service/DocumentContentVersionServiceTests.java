package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentContentVersionServiceTests {

    @Mock
    DocumentContentVersionRepository documentContentVersionRepository;

    @Mock
    StoredDocumentRepository storedDocumentRepository;


    DocumentContentVersionService documentContentVersionService;

    @BeforeEach
    public void setUp() throws Exception {
        documentContentVersionService = new DocumentContentVersionService(documentContentVersionRepository, storedDocumentRepository);
    }

    @Test
    public void testFindOne() {
        when(documentContentVersionRepository.findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(new DocumentContentVersion()));
        assertThat(documentContentVersionService.findById(TestUtil.RANDOM_UUID)).isNotNull();
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileId() {
        when(storedDocumentRepository
            .findByIdAndDeleted(TestUtil.RANDOM_UUID, false))
                .thenReturn(Optional.of(TestUtil.STORED_DOCUMENT));
        assertThat(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID))
            .isEqualTo(Optional.of(TestUtil.STORED_DOCUMENT.getMostRecentDocumentContentVersion()));
    }

    @Test
    public void testMostRecentFileContentVersionByStoredFileIdOnNullStoredFile() {
        when(storedDocumentRepository.findByIdAndDeleted(TestUtil.RANDOM_UUID, false)).thenReturn(Optional.empty());
        assertThat(documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(TestUtil.RANDOM_UUID))
            .isEqualTo(Optional.empty());
    }

}
