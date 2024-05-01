package uk.gov.hmcts.dm.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtilsTests {

    @Test
    public void testVariousScenarios() {

        String sanitised = StringUtils.sanitiseFileName(null);
        Assert.assertNull(sanitised);

        sanitised = StringUtils.sanitiseFileName("±!@£$%^&*()_+}{|\"':?><~abc");
        Assert.assertEquals("_+abc", sanitised);

        sanitised = StringUtils.sanitiseFileName("marriage-certificate.png");
        Assert.assertEquals("marriage-certificate.png", sanitised);

        sanitised = StringUtils.sanitiseFileName("marriage-certificate<script>alert(1)</script>.png");
        Assert.assertEquals("marriage-certificatescriptalert1script.png", sanitised);

    }

    @Test
    public void testSanitiseUserRoles() {

        String sanitised = StringUtils.sanitiseUserRoles(null);
        Assert.assertNull(sanitised);

        sanitised = StringUtils.sanitiseUserRoles("±!@£$%^&*()_+}{|\"':?><~abc");
        Assert.assertEquals("_+abc", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("marriage-certificate.png");
        Assert.assertEquals("marriage-certificate.png", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("marriage-certificate<script>alert(1)</script>.png");
        Assert.assertEquals("marriage-certificatescriptalert1script.png", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("22caseworker,caseworker-sscs01");
        Assert.assertEquals("22caseworker,caseworker-sscs01", sanitised);
    }

    @Test
    public void convertValidLog() {
        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String safeLogStr = "this  is  an  apple  .";
        Assert.assertNotEquals(dangerousLogStr, safeLogStr);
        Assert.assertEquals(safeLogStr, StringUtils.convertValidLogString(dangerousLogStr));
    }

    @Test
    public void convertValidLogNonEmptyList() {

        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String dangerousLogStr2 = "this %0d is \r an %0a mango \n .";
        String safeLogStr = "this  is  an  apple  .";
        Set<String> initialSet = new HashSet<>(Arrays.asList(dangerousLogStr, dangerousLogStr2));

        Set<String> sanitisedSet = StringUtils.convertValidLogStrings(initialSet);

        Assert.assertEquals(initialSet.size(), sanitisedSet.size());
        Assert.assertEquals(safeLogStr, sanitisedSet.stream().findFirst().get());
    }
}
