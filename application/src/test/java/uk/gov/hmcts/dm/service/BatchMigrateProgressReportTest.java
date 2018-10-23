package uk.gov.hmcts.dm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.time.Duration;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BatchMigrateProgressReportTest {

    MigrateProgressReport beforeJob;
    List<DocumentContentVersion> migratedDocumentContentVersions;
    MigrateProgressReport afterJob;
    Duration duration;

    @Before
    public void setUp() {
        beforeJob = new MigrateProgressReport(100L, 10L);
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(fromString("d46dad43-338b-43c5-a4ff-9a6a9bb97ccd"));

        DocumentContentVersion dcv1 = new DocumentContentVersion();
        dcv1.setId(fromString("e3ef03bc-1ae4-43ca-9025-0cbbb223988d"));
        dcv1.setStoredDocument(storedDocument);


        DocumentContentVersion dcv2 = new DocumentContentVersion();
        dcv2.setId(fromString("0f56dd3f-2dd2-4d4a-a5a3-ba3cf9e70679"));
        dcv2.setStoredDocument(storedDocument);
        dcv2.setContentChecksum("IssmJFStAR");
        dcv2.setContentUri("http://matters.nomore/really");

        migratedDocumentContentVersions = asList(dcv1, dcv2);

        afterJob = new MigrateProgressReport(120L, 30L);
        duration = Duration.ofMillis(4005L);
    }

    @Test
    public void toJson() throws Exception {


        BatchMigrateProgressReport report = new BatchMigrateProgressReport(beforeJob,
                                                                           migratedDocumentContentVersions,
                                                                           afterJob,
                                                                           duration);

        final String json = new ObjectMapper().writeValueAsString(report);
        assertThat(json, is("{\"before_job\":{\"left_to_migrate\":100,\"migrated\":10},"
                                + "\"migrated\":[{\"document_id\":\"d46dad43-338b-43c5-a4ff-9a6a9bb97ccd\","
                                + "\"version_id\":\"e3ef03bc-1ae4-43ca-9025-0cbbb223988d\",\"uri\":null,"
                                + "\"checksum\":null},{\"document_id\":\"d46dad43-338b-43c5-a4ff-9a6a9bb97ccd\","
                                + "\"version_id\":\"0f56dd3f-2dd2-4d4a-a5a3-ba3cf9e70679\",\"uri\":\"http://matters"
                                + ".nomore/really\",\"checksum\":\"IssmJFStAR\"}],"
                                + "\"after_job\":{\"left_to_migrate\":120,\"migrated\":30},\"status\":\"OK\","
                                + "\"duration\":\"4s, 5ms\"}"));
    }
}
