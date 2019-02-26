package uk.gov.hmcts.dm.dialect;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

public class PassThroughBlobTests {

    @Test(expected = UnsupportedOperationException.class)
    public void test_getBinaryStream() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.getBinaryStream(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBinaryStream() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBinaryStream(0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_getBytes() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.getBytes(0L, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBytes() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBytes(0L, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBytesOverloaded() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBytes(0L, null, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_Position() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.position(new byte[] {  }, 0L);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_PositionBlob() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.position(new PassThroughBlob(in), 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_truncate() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.truncate(0L);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_free() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.free();
    }


    @Test
    public void test_hashCode() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in, 1L);
        PassThroughBlob blob2 = new PassThroughBlob(in, 1L);
        Assert.assertEquals(blob2.hashCode(), blob.hashCode());
    }

    @Test
    public void test_Equals() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in, 1L);
        PassThroughBlob blob2 = new PassThroughBlob(in, 1L);
        Assert.assertTrue(blob.equals(blob2));
    }
}
