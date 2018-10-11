package uk.gov.hmcts.dm.dialect;

import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

public class ByteWrappingBlobTypeTests {

    ByteWrappingBlobType type = new ByteWrappingBlobType();

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

    @Test(expected = HibernateException.class)
    public void testDeepCopy() throws Exception {
        PassThroughBlob blob = Mockito.mock(PassThroughBlob.class);
        Mockito.when(blob.getBinaryStream()).thenThrow(new SQLException("xxx"));
        type.deepCopy(blob);
    }

    @Test
    public void testDeepCopyNull() throws Exception {
        Assert.assertEquals(null, type.deepCopy(null));
    }


}
