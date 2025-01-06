package uk.gov.hmcts.dm.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringUtilsTests {

    @Test
    void testVariousScenarios() {

        String sanitised = StringUtils.sanitiseFileName(null);
        assertNull(sanitised);

        sanitised = StringUtils.sanitiseFileName("±!@£$%^&*()_+}{|\"':?><~abc");
        assertEquals("_+abc", sanitised);

        sanitised = StringUtils.sanitiseFileName("marriage-certificate.png");
        assertEquals("marriage-certificate.png", sanitised);

        sanitised = StringUtils.sanitiseFileName("marriage-certificate<script>alert(1)</script>.png");
        assertEquals("marriage-certificatescriptalert1script.png", sanitised);

    }

    @Test
    void testSanitiseUserRoles() {

        String sanitised;

        sanitised = StringUtils.sanitiseUserRoles("±!@£$%^&*()_+}{|\"':?><~abc");
        assertEquals("_+abc", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("marriage-certificate.png");
        assertEquals("marriage-certificate.png", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("marriage-certificate<script>alert(1)</script>.png");
        assertEquals("marriage-certificatescriptalert1script.png", sanitised);

        sanitised = StringUtils.sanitiseUserRoles("22caseworker,caseworker-sscs01");
        assertEquals("22caseworker,caseworker-sscs01", sanitised);
    }

    @Test
    void convertValidLog() {
        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String safeLogStr = "this  is  an  apple  .";
        assertNotEquals(dangerousLogStr, safeLogStr);
        assertEquals(safeLogStr, StringUtils.convertValidLogString(dangerousLogStr));
    }

    @Test
    void convertValidLogNonEmptyList() {

        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String dangerousLogStr2 = "this %0d is \r an %0a mango \n .";
        String safeLogStr = "this  is  an  apple  .";
        Set<String> initialSet = new HashSet<>(Arrays.asList(dangerousLogStr, dangerousLogStr2));

        Set<String> sanitisedSet = StringUtils.convertValidLogStrings(initialSet);

        assertEquals(initialSet.size(), sanitisedSet.size());
        assertEquals(safeLogStr, sanitisedSet.stream().findFirst().get());
    }
}
