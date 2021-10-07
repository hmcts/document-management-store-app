package uk.gov.hmcts.dm.dialect;

import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteWrappingBlobTypeTests {

    ByteWrappingBlobType type = new ByteWrappingBlobType();

    @Mock
    Blob blob;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    InputStream inputStream;

    @Mock
    ResultSet resultSet;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReturnedClass() {
        Assert.assertEquals(type.returnedClass(), PassThroughBlob.class);
    }

    @Test
    public void testEquals() {
        Integer a = 1;
        Integer b = 1;
        Integer c = 1;
        Assert.assertTrue(type.equals(a,b));
        Assert.assertTrue(type.equals(a,c));
        Assert.assertFalse(type.equals(a,null));
        Assert.assertFalse(type.equals(null,b));
        Assert.assertFalse(type.equals(null,null));
    }

    @Test
    public void testHashCode() {
        Integer a = 1;
        Assert.assertEquals(type.hashCode(a), a.hashCode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDisassemble() {
        type.disassemble(new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAssemble() {
        type.assemble(1L, new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReplace() {
        type.replace(new Object(), new Object(), new Object());
    }

    @Test
    public void testDeepCopy() throws Exception {
        PassThroughBlob passThroughBlob = (PassThroughBlob) type.deepCopy(blob);
        Mockito.verify(blob, Mockito.atLeast(1)).getBinaryStream();
    }

    @Test(expected = HibernateException.class)
    public void testDeepCopyThrowsException() throws Exception {
        PassThroughBlob blob = Mockito.mock(PassThroughBlob.class);
        Mockito.when(blob.getBinaryStream()).thenThrow(new SQLException("xxx"));
        type.deepCopy(blob);
    }

    @Test
    public void testDeepCopyNull() throws Exception {
        Assert.assertEquals(null, type.deepCopy(null));
    }

    @Test
    public void testNullSafeSet() throws SQLException {
        Mockito.when(blob.getBinaryStream()).thenReturn(inputStream);
        type.nullSafeSet(preparedStatement, blob, 1, null);
        Mockito.verify(preparedStatement, Mockito.atLeast(1)).setBinaryStream(1, inputStream, blob.length());
    }

    @Test(expected = HibernateException.class)
    public void testNullSafeSetThrowsExcpetion() throws SQLException {
        Mockito.when(blob.getBinaryStream()).thenThrow(new SQLException());
        type.nullSafeSet(preparedStatement, blob, 1, null);
    }

    @Test
    public void testNullSafeGet() throws SQLException {
        Mockito.when(blob.getBinaryStream()).thenReturn(inputStream);
        String[] names = {"first", "second"};
        type.nullSafeGet(resultSet, names, null, null);
        Mockito.verify(resultSet, Mockito.atLeast(1)).getBinaryStream("first");
    }
}
