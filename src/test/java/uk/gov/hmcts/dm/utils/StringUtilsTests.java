package uk.gov.hmcts.dm.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTests {

    @Test
    public void testVariousScenarios() {

        String sanitised = StringUtils.sanitiseFileName(null);
        Assert.assertEquals(sanitised, null);

        sanitised = StringUtils.sanitiseFileName("±!@£$%^&*()_+}{|\"':?><~abc");
        Assert.assertEquals(sanitised, "_+abc");

        sanitised = StringUtils.sanitiseFileName("marriage-certificate.png");
        Assert.assertEquals(sanitised, "marriage-certificate.png");

        sanitised = StringUtils.sanitiseFileName("marriage-certificate<script>alert(1)</script>.png");
        Assert.assertEquals(sanitised, "marriage-certificatescriptalert1script.png");

    }

    @Test
    public void convertValidLog() {
        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String safeLogStr = "this  is  an  apple  .";
        Assert.assertNotEquals(dangerousLogStr, safeLogStr);
        Assert.assertEquals(safeLogStr, StringUtils.convertValidLogString(dangerousLogStr));
    }

}
