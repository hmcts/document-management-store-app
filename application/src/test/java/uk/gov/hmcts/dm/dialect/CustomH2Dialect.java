package uk.gov.hmcts.dm.dialect;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class CustomH2Dialect extends H2Dialect {

    public CustomH2Dialect() {
        super();
        // use "bytea" to map blob types
        registerColumnType(Types.BLOB, "bytea");
        // turn on blob mapping
        getDefaultProperties().setProperty(ByteWrappingBlobType.MAP_BLOBS_TO_BINARY_TYPE, String.valueOf(true));
    }
}
