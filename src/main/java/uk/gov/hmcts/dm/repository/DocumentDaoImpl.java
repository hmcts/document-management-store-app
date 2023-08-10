package uk.gov.hmcts.dm.repository;

import jakarta.persistence.EntityManager;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.DocumentContentVersionAuditEntry;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.mapper.DocumentContentVersionMapper;
import uk.gov.hmcts.dm.domain.mapper.StoredDocumentMapper;
import uk.gov.hmcts.dm.generated.db.tables.Auditentry;
import uk.gov.hmcts.dm.generated.db.tables.records.AuditentryRecord;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Repository
public class DocumentDaoImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public StoredDocument getStoredDocument(UUID id) {
        try {
            String query = "SELECT * FROM STOREDDOCUMENT WHERE ID = ?";
            StoredDocument storedDocument = jdbcTemplate.queryForObject(
                query, new StoredDocumentMapper(), id);
            return storedDocument;
        } catch (EmptyResultDataAccessException e) {
            return null; //Implies there is no record for given Id.
        }
    }

    //Below will require an Index to added for the storeddocument_id column
    public DocumentContentVersion getRecentDocumentContentVersion(UUID storeddocumentId) {

        //Get only the latest version based on createdOn date with minimum required fields only.

        try {
            String sql = "SELECT * FROM DocumentContentVersion WHERE storeddocument_id = ? ORDER BY createdOn DESC LIMIT 1";
            DocumentContentVersion documentContentVersion = jdbcTemplate.queryForObject(
                sql, new DocumentContentVersionMapper(), storeddocumentId);

            return documentContentVersion;
        } catch (EmptyResultDataAccessException e) {
            return null; //Implies there is no record for given Id.
        }
    }

    public DocumentContentVersionAuditEntry createAndSaveDocumentContentVersionAuditEntry(
        DocumentContentVersion documentContentVersion, String username,
            String serviceName, AuditActions action, UUID documentId) {

        DocumentContentVersionAuditEntry documentContentVersionAuditEntry = new DocumentContentVersionAuditEntry();
        //entityManager.persist(documentContentVersionAuditEntry);

        DSLContext dslContext = DSL.using(dbUrl);
        AuditentryRecord auditentryRecord = dslContext.newRecord(Auditentry.AUDITENTRY);
        auditentryRecord.setAction(action.toString());
        auditentryRecord.setUsername(username);
        auditentryRecord.setServicename(serviceName);
        auditentryRecord.setDocumentcontentversionId(documentContentVersion.getId());
        auditentryRecord.setStoreddocumentId(documentId);
        auditentryRecord.setType("document_content_version");
        auditentryRecord.setRecordeddatetime(LocalDateTime.now());
        //auditentryRecord.setId(documentContentVersionAuditEntry.getId());
        //dslContext.insertInto(Auditentry.AUDITENTRY).set(auditentryRecord).execute();
        //entityManager.detach(documentContentVersionAuditEntry);
        //entityManager.clear();
        auditentryRecord.store();



//        documentContentVersionAuditEntry.setAction(action);
//        documentContentVersionAuditEntry.setUsername(username);
//        documentContentVersionAuditEntry.setServiceName(serviceName);
//        documentContentVersionAuditEntry.setRecordedDateTime(new Date());
//        documentContentVersionAuditEntry.setDocumentContentVersion(documentContentVersion);
//        documentContentVersionAuditEntry.setStoredDocument(getStoredDocument(documentId));
//
//        dslContext.insertInto(Auditentry.AUDITENTRY).set(documentContentVersionAuditEntry);

        return null;
    }


}
