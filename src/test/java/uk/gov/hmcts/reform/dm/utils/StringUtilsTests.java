package uk.gov.hmcts.reform.dm.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pawel on 04/10/2017.
 */
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

}
