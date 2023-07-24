package uk.gov.hmcts.dm.domain.mapper;

import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DocumentContentVersionMapper implements RowMapper<DocumentContentVersion> {
    @Override
    public DocumentContentVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
        DocumentContentVersion documentContentVersion=  new DocumentContentVersion(
            UUID.fromString(rs.getString("id")),
            rs.getString("mimeType"),
            rs.getString("originalDocumentName"),
            rs.getString("createdBy"),
            rs.getString("createdByService"),
            rs.getDate("createdOn"),
            null,
            null,
            rs.getLong("size"),
            rs.getString("content_uri"),
            rs.getString("content_checksum")
        );

        return documentContentVersion;
    }
}
