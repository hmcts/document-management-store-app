package uk.gov.hmcts.dm.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.BatchMigrationAuditEntry;
import uk.gov.hmcts.dm.repository.BatchMigrationAuditEntryRepository;

import java.time.Duration;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class BatchMigrationAuditEntryServiceTest {

    private BatchMigrationAuditEntryService underTest;
    @Mock
    private BatchMigrationAuditEntryRepository batchMigrationAuditEntryRepository;
    @Mock
    private BatchMigrationAuditEntry audit;

    @Before
    public void setUp() {
        underTest = new BatchMigrationAuditEntryService(batchMigrationAuditEntryRepository);
    }

    @Test
    public void createAuditEntry() {
        underTest.createAuditEntry("authToken", 100, false);
        verify(batchMigrationAuditEntryRepository).save(argThat(new BatchMigrationAuditEntryMatcher("authToken", 100, false)));
    }

    @Test
    public void save() {
        final MigrateProgressReport migrateProgressReport = new MigrateProgressReport(1L, 1L);
        final BatchMigrateProgressReport report = new BatchMigrateProgressReport(migrateProgressReport,
                                                                                 emptyList(),
                                                                                 migrateProgressReport,
                                                                                 Duration.ofMillis(30));
        underTest.save(audit, report);
        verify(batchMigrationAuditEntryRepository).save(audit);
    }

    @Test
    public void withoutSave() {
        underTest.save(audit, new NoJsonBatchMigrateProgressReport());
        verifyZeroInteractions(batchMigrationAuditEntryRepository);
    }

    class BatchMigrationAuditEntryMatcher extends ArgumentMatcher<BatchMigrationAuditEntry> {

        private final String authToken;
        private final Integer batchSize;
        private final Boolean mockRun;

        BatchMigrationAuditEntryMatcher(final String authToken, final Integer batchSize, final Boolean mockRun) {
            this.authToken = authToken;
            this.batchSize = batchSize;
            this.mockRun = mockRun;
        }

        @Override
        public boolean matches(final Object item) {
            BatchMigrationAuditEntry other = (BatchMigrationAuditEntry)item;
            return StringUtils.equals(authToken, other.getMigrationKey())
                && batchSize == other.getBatchSize()
                && mockRun == other.getMockRun();
        }
    }

    class NoJsonBatchMigrateProgressReport extends BatchMigrateProgressReport {
        NoJsonBatchMigrateProgressReport() {
            super(null, emptyList(), null, null);
        }
    }
}
