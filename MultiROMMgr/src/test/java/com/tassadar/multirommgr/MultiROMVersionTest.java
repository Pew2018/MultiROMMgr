package com.tassadar.multirommgr;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class MultiROMVersionTest {
    @Test public void preservesLegacyVersions() {
        assertArrayEquals(new int[] {19, 'g'}, MultiROM.parseMultiRomVersions("19g"));
        assertArrayEquals(new int[] {33, 0}, MultiROM.parseMultiRomVersions("33"));
        assertArrayEquals(new int[] {33, 'e'}, MultiROM.parseMultiRomVersions("33e"));
    }
    @Test public void handlesPortBuildSuffixes() {
        assertArrayEquals(new int[] {33, 0}, MultiROM.parseMultiRomVersions("v33-UNOFFICIAL"));
        assertArrayEquals(new int[] {33, 'e'}, MultiROM.parseMultiRomVersions("33e_20170212"));
    }
    @Test public void malformedVersionsFailClosed() {
        for (String value : new String[] {null, "", " ", "unknown", "33junk", "999999999999999999"}) {
            assertArrayEquals(new int[] {0, 0}, MultiROM.parseMultiRomVersions(value));
        }
    }
}
