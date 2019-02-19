package uk.gov.hmcts.dm.dialect;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL94Dialect;

public class CustomPostgresSqlDialect extends PostgreSQL94Dialect {
    public CustomPostgresSqlDialect() {
        super();
        // use "bytea" to map blob types
        registerColumnType(Types.BLOB, "bytea");
        // turn on blob mapping
        getDefaultProperties().setProperty(ByteWrappingBlobType.MAP_BLOBS_TO_BINARY_TYPE, String.valueOf(true));
    }
}
