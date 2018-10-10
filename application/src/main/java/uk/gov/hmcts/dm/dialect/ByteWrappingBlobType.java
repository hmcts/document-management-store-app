package uk.gov.hmcts.dm.dialect;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.*;

public class ByteWrappingBlobType implements UserType {

    public static final String MAP_BLOBS_TO_BINARY_TYPE = "mapBlobsToBinaryType";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.BLOB};
    }

    @Override
    public Class returnedClass() {
        return PassThroughBlob.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        if (x != null) {
            return x.equals(y);
        } else if (y != null) {
            return y.equals(x);
        } else {
            return false;
        }


    }

    @Override
    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
        InputStream inputStream = rs.getBinaryStream(names[0]);
        return new PassThroughBlob(inputStream, 0L);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
        try {
            Blob b = (Blob) value;
            st.setBinaryStream(index, b.getBinaryStream(), b.length());
        } catch (SQLException e) {
            throw new HibernateException("Could not nullSafeSet", e);
        }

    }

    @Override
    public Object deepCopy(Object value)  {
        if (value != null) {
            try {
                Blob blob = (Blob) value;
                return new PassThroughBlob(blob.getBinaryStream(), blob.length());
            } catch (SQLException e) {
                throw new HibernateException("Could not deepCopy", e);
            }

        }
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value)  {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object assemble(Serializable cached, Object owner)  {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object replace(Object original, Object target, Object owner)  {
        throw new UnsupportedOperationException();
    }

}
