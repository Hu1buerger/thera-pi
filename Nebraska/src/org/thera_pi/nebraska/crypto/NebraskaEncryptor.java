package org.thera_pi.nebraska.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This object can be used to encrypt data with the certificate of a specified
 * receiver. Optionally it can add an encryption using the sender's certificate
 * to allow decryption by the sender.
 * 
 * General use case:
 * - Get a NebraskaEncryptor for the recipient's ID (IK) from the 
 * NebraskaKeystore object.
 * - Optionally use setEncryptToSelf(true) to be able to decrypt the data. 
 * Otherwise only the specified recipient is able to decrypt it.
 * - Use one of the encrypt functions to encrypt the data.
 *
 * @author bodo
 *
 */
public class NebraskaEncryptor {
    private String receiverIK;
    private X509Certificate receiverCert;
    private X509Certificate senderCert;
    private PrivateKey senderKey;
    private CertStore certificateChain;
    private boolean encryptToSelf;
    private String signatureAlgorithm2use;

    public boolean isEncryptToSelf() {
        return encryptToSelf;
    }

    public void setEncryptToSelf(boolean encryptToSelf) {
        this.encryptToSelf = encryptToSelf;
    }

    /**
     * Create a Nebraska encryptor for specified receiver.
     *
     * @param IK               receiver ID (IK)
     * @param nebraskaKeystore reference to NebraskaKeystore object for access to
     *                         the keys
     * @throws NebraskaCryptoException         on cryptography related errors
     * @throws NebraskaNotInitializedException if institution ID, institution name
     */
    NebraskaEncryptor(String IK, NebraskaKeystore nebraskaKeystore)
            throws NebraskaCryptoException, NebraskaNotInitializedException {
        receiverIK = NebraskaUtil.normalizeIK(IK);
        receiverCert = nebraskaKeystore.getCertificate(receiverIK);
        senderKey = nebraskaKeystore.getSenderKey();
        senderCert = nebraskaKeystore.getSenderCertificate();
        certificateChain = nebraskaKeystore.getSenderCertChain();
        signatureAlgorithm2use = nebraskaKeystore.getCertSignatureAlgorithm(); 
    }

    /**
     * Sign and encrypt data from input file and write result to output file.
     *
     * @param inFileName  input file
     * @param outFileName output file
     * @return size of resulting output file
     * @throws NebraskaCryptoException on cryptography related errors
     * @throws NebraskaFileException   on I/O related errors
     */
    public long encrypt(String inFileName, String outFileName) throws NebraskaCryptoException, NebraskaFileException {
        InputStream inStream;
        OutputStream outStream;
        File outFile;
        try {
            inStream = new FileInputStream(inFileName);
            outFile = new File(outFileName);
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            throw new NebraskaFileException(e);
        }
        encrypt(inStream, outStream);
        try {
            outStream.close();
            inStream.close();
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }
        return outFile.length();
    }

    /**
     * Sign and encrypt data from input stream and write to output stream. Input
     * stream will be copied to a byte array using a ByteArrayOutputStream for
     * further processing.
     *
     * @param inStream  plain text data stream
     * @param outStream encrypted data stream
     * @throws NebraskaCryptoException on cryptography related errors
     * @throws NebraskaFileException   on I/O related errors
     */
    public void encrypt(InputStream inStream, OutputStream outStream)
            throws NebraskaCryptoException, NebraskaFileException {
        /*
         * To get the input as byte array we copy all data to a ByteArrayOutputStream
         * and retrieve the byte array from it.
         */
        Provider provBC = Security.getProvider(NebraskaConstants.SECURITY_PROVIDER);
        Provider bcProvider = null;

        if (provBC == null) {
            bcProvider = new BouncyCastleProvider();
            Security.addProvider(bcProvider);
        } else {
            bcProvider = provBC;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        CMSProcessable plainContent;
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                byteStream.write(buffer, 0, len);
            }
            byteStream.flush();

            // generate needs a CMSProcessable
            plainContent = new CMSProcessableByteArray(byteStream.toByteArray());
            byteStream.close();
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }

        // first processing step: sign data

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        String digestOID = (getDigest());
        switch (signatureAlgorithm2use) {
        case "SHA1WithRSAEncryption":
        case "1.3.14.3.2.26":
        case "SHA256WithRSAEncryption":
        case "2.16.840.1.101.3.4.2.1":
            generator.addSigner(senderKey, senderCert, digestOID);
            break;
        case "SHA256WithRSAandMGF1":
        case "1.2.840.113549.1.1.10":
            generator.addSigner(senderKey, senderCert, CMSSignedDataGenerator.ENCRYPTION_RSA_PSS, digestOID);
            break;
        default:
           System.out.println("NebraskaEncryptor.getDigest: unknown SignatureAlgorithm: " + signatureAlgorithm2use);
        }

        try {
            generator.addCertificatesAndCRLs(certificateChain);
        } catch (CertStoreException e) {
            throw new NebraskaCryptoException(e);
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        }

        CMSSignedData signedData;
        try {
            signedData = generator.generate(plainContent, true, NebraskaConstants.SECURITY_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            throw new NebraskaCryptoException(e);
        } catch (NoSuchProviderException e) {
            throw new NebraskaCryptoException(e);
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        }

        // DER encoded output
        byte[] encodedSignedData = null;
        try {
            encodedSignedData = signedData.getEncoded();
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }

        // second processing step: encrypt data

        CMSEnvelopedDataGenerator envelopedGenerator = new CMSEnvelopedDataGenerator();

        // the receiver must be able to decrypt the data
        envelopedGenerator.addKeyTransRecipient(receiverCert);

        // optionally the sender may also decrypt it
        if (encryptToSelf) {
            envelopedGenerator.addKeyTransRecipient(senderCert);
        }

        CMSProcessable signedContent;
        signedContent = new CMSProcessableByteArray(encodedSignedData);
        CMSEnvelopedData envelopedData;
        try {
            envelopedData = envelopedGenerator.generate(signedContent, CMSEnvelopedDataGenerator.AES256_CBC,
                    NebraskaConstants.SECURITY_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            throw new NebraskaCryptoException(e);
        } catch (NoSuchProviderException e) {
            throw new NebraskaCryptoException(e);
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        }
        byte[] encodedEnvelopedData;
        try {
            encodedEnvelopedData = envelopedData.getEncoded();
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }

        // write result to output
        try {
            outStream.write(encodedEnvelopedData);
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }
    }

    private String getDigest() {
        String retVal = CMSSignedDataGenerator.DIGEST_SHA256;
        switch (signatureAlgorithm2use) {
        case "SHA1WithRSAEncryption":
        case "1.3.14.3.2.26":
            retVal = CMSSignedDataGenerator.DIGEST_SHA1;
            break;
        case "SHA256WithRSAEncryption":
        case "2.16.840.1.101.3.4.2.1":
        case "SHA256WithRSAandMGF1":    // Hash ist auch SHA256
        case "1.2.840.113549.1.1.10":
            // default bleibt
            break;
        default:
           System.out.println("NebraskaEncryptor.getDigest: unknown SignatureAlgorithm: " + signatureAlgorithm2use + ", use default");
        }
        return retVal;
    }

}
