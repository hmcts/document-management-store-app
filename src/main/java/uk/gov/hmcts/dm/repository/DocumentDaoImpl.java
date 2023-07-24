package uk.gov.hmcts.dm.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.mapper.DocumentContentVersionMapper;
import uk.gov.hmcts.dm.domain.mapper.StoredDocumentMapper;

import java.util.UUID;

@Repository
public class DocumentDaoImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public StoredDocument getStoredDocument(UUID id) {

        String query = "SELECT * FROM STOREDDOCUMENT WHERE ID = ?";
        StoredDocument storedDocument = jdbcTemplate.queryForObject(
            query, new Object[] { id }, new StoredDocumentMapper());
        return storedDocument;
    }

    public DocumentContentVersion getDocumentContentVersion(UUID id) {

        String query = "SELECT * FROM STOREDDOCUMENT WHERE ID = ?";
        DocumentContentVersion documentContentVersion = jdbcTemplate.queryForObject(
            query, new Object[] { id }, new DocumentContentVersionMapper());
        return documentContentVersion;
    }
}
