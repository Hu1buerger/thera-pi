package crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class VerschluesselnTest {
    final private transient static String password = "jeLaengerJeBesserPasswortRehaVerwaltung";

    @Test
    public void testGetInstance() {
        assertNotSame(Verschluesseln.getInstance(), Verschluesseln.getInstance());
    }

    @Test
    public void testEncrypt() {
        Verschluesseln ver = Verschluesseln.getInstance();
        assertEquals("OK1VGAz2LN15juNtHxkcbg==", ver.encrypt("Bastie"));
        assertEquals("OK1VGAz2LN0M6y0ozt7lVkY4qMZrBXX/gztR+1m/CtP4NW8aP2AMjHIwEaci2J3AuHqzdnsrKwq9" + "g2j9dbigyQ==",
                ver.encrypt("Bastie_hateinengroßenH\u00fctehund"));
        assertEquals("E2fyCxAPQkqXjpVUetngA3H8rkblqjY8", ver.encrypt("therapi"));
        assertEquals("E2fyCxAPQkqXjpVUetngA6TjLd3z7xZq", ver.encrypt("therapie"));



    }

    @Test
    public void testDecrypt() throws SecurityException {
        Verschluesseln ver = Verschluesseln.getInstance();
        assertEquals("Bastie", ver.decrypt("OK1VGAz2LN15juNtHxkcbg=="));
        assertEquals("Bastie_hateinengroßenH\u00fctehund", ver.decrypt(
                "OK1VGAz2LN0M6y0ozt7lVkY4qMZrBXX/gztR+1m/CtP4NW8aP2AMjHIwEaci2J3AuHqzdnsrKwq9\r\n" + "g2j9dbigyQ=="));
    }

    @Test
    public void testEncryptDecrypt() {
        Verschluesseln ver = Verschluesseln.getInstance();
        assertEquals("Bastie_hateinengroßenH\u00fctehund", ver.decrypt(ver.encrypt("Bastie_hateinengroßenHütehund")));

        assertEquals("Bastie_hateinengroßenH\u00fctehund",
                ver.decrypt(ver.encrypt("Bastie_hateinengroßenH\u00fctehund")));
        assertEquals("Bastie", ver.decrypt(ver.encrypt("Bastie")));

    }

    @Test
    public void testDecryptencrypt() {
        Verschluesseln ver = Verschluesseln.getInstance();

        assertEquals("OK1VGAz2LN15juNtHxkcbg==", ver.encrypt(ver.decrypt("OK1VGAz2LN15juNtHxkcbg==")));
        assertEquals("OK1VGAz2LN0M6y0ozt7lVkY4qMZrBXX/gztR+1m/CtP4NW8aP2AMjHIwEaci2J3AuHqzdnsrKwq9" + "g2j9dbigyQ==",
                ver.encrypt(
                        ver.decrypt("OK1VGAz2LN0M6y0ozt7lVkY4qMZrBXX/gztR+1m/CtP4NW8aP2AMjHIwEaci2J3AuHqzdnsrKwq9\r\n"
                                + "g2j9dbigyQ==")));
    }

    @Test
    public void testGetPassword() {
        Verschluesseln ver = Verschluesseln.getInstance();
        assertEquals(password, ver.getPassword());

        Verschluesseln verBlubb = Verschluesseln.getInstance("blubb");
        assertEquals("blubb", verBlubb.getPassword());

    }

}
