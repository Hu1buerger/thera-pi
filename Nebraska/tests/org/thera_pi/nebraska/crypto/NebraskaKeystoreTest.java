package org.thera_pi.nebraska.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class NebraskaKeystoreTest {
    private static final String personName = "Max Mustermann";
    private static final String institutionName = "Test Institution";
    private static final String institutionId = "IK123456789";
    private static final String keyPassword = "abcdef";
    private static final String keystorePassword = "123456";
    private static final String keystoreFilename = "/tmp/keystore.p12";
    private static final String requestFilename = "/tmp/request.p10";

    private NebraskaKeystore nebraskaKeystore;

    @Before
    public void setUp() throws Exception {
        File keystoreFile = new File(keystoreFilename);
        if (keystoreFile.exists()) {
            keystoreFile.delete();
        }
        nebraskaKeystore = new NebraskaKeystore(keystoreFilename, keystorePassword, keyPassword, institutionId,
                institutionName, personName);
    }

    @Test
    public void testGenerateKeyPair()
            throws NebraskaCryptoException, NebraskaFileException, NebraskaNotInitializedException {
        assertFalse(nebraskaKeystore.hasPrivateKey());
        nebraskaKeystore.generateKeyPair(true);
        assertTrue(nebraskaKeystore.hasPrivateKey());
    }

    @Test
    public void testCreateCertificateRequest()
            throws NebraskaCryptoException, NebraskaFileException, IOException, NebraskaNotInitializedException {
        StringBuffer md5Hash = new StringBuffer();
        assertFalse(nebraskaKeystore.hasPrivateKey());
        nebraskaKeystore.generateKeyPair(true);
        assertTrue(nebraskaKeystore.hasPrivateKey());

        File requestFile = new File(requestFilename);
        if (requestFile.exists()) {
            requestFile.delete();
        }
        FileOutputStream requestStream = new FileOutputStream(requestFile);
        nebraskaKeystore.createCertificateRequest(requestStream, md5Hash);
        requestStream.close();

        assertTrue(requestFile.exists());
//        assertEquals("", md5Hash.toString());
    }

    @Test
    public void testGetPublicKeyMD5()
            throws NebraskaCryptoException, NebraskaFileException, NebraskaNotInitializedException {
        assertFalse(nebraskaKeystore.hasPrivateKey());
        nebraskaKeystore.generateKeyPair(true);
        assertTrue(nebraskaKeystore.hasPrivateKey());

        String fingerprint = nebraskaKeystore.getPublicKeyMD5();
        assertNotNull(fingerprint);
        assertEquals(fingerprint, nebraskaKeystore.getPublicKeyMD5());
        assertTrue(fingerprint.length() == 47);
        // FIXME find a way to really check the fingerprint
    }

}
