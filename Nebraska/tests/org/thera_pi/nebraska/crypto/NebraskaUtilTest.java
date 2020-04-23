package org.thera_pi.nebraska.crypto;

import org.junit.Test;

import junit.framework.TestCase;

public class NebraskaUtilTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetSubjectDN() {
        assertEquals(
                "CN=Max Mustermann, OU=IK123456789, OU=Test Institution, O=ITSG TrustCenter fuer sonstige Leistungserbringer, C=DE",
                NebraskaUtil.getSubjectDN("123456789", "Test Institution", "Max Mustermann"));
        assertEquals(
                "CN=Wäänä Brösel, OU=IK000123456, OU=Łógòpädie Sèmméłweiß, O=ITSG TrustCenter fuer sonstige Leistungserbringer, C=DE",
                NebraskaUtil.getSubjectDN("IK 000123456", "Łógòpädie Sèmméłweiß", "Wäänä Brösel"));
    }

    @Test
    public void testNormalizeDnField() {
        assertEquals("Meissen", NebraskaUtil.normalizeDnField("Meißen"));
        assertEquals("alpha beta gamma", NebraskaUtil.normalizeDnField("alpha,beta,gamma"));
        assertEquals("aens zwo drei", NebraskaUtil.normalizeDnField("æns, zwø, dreí"));
    }

    @Test
    public void testNormalizeIK() {
        assertEquals("123456789", NebraskaUtil.normalizeIK("123456789"));
        assertEquals("100200300", NebraskaUtil.normalizeIK("IK100200300"));
        assertEquals("111222333", NebraskaUtil.normalizeIK("IK 111222333"));
    }

}
