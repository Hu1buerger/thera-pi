package org.thera_pi.nebraska.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaAlgorithmParametersConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

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
    private JcaCertStore certificateChain;
    private boolean encryptToSelf;
    private String signatureAlgorithm2use;
    private boolean useRSAES;

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
        useRSAES = (nebraskaKeystore.getOwnCertLength() >= 4096);
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
    	
    	Provider provBC = Security.getProvider(NebraskaConstants.SECURITY_PROVIDER);
        Provider bcProvider = null;

        if (provBC == null) {
            bcProvider = new BouncyCastleProvider();
            Security.addProvider(bcProvider);
        } else {
            bcProvider = provBC;
        }
        
        /*
         * To get the input as byte array we copy all data to a ByteArrayOutputStream
         * and retrieve the byte array from it.
         */                
        byte[] encodedSignedData = null;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
        	byte[] buffer = new byte[1024];
        	int len;
        	while ((len = inStream.read(buffer)) > 0) {
        		byteStream.write(buffer, 0, len);
        	}
        	byteStream.flush();

            // sign the data
        	encodedSignedData = this.signData(byteStream.toByteArray());
        	byteStream.close();
        } catch (IOException e) {
        	throw new NebraskaFileException(e);
        }

        // second processing step: encrypt data
        byte[] encodedEnvelopedData = null;
        encodedEnvelopedData = this.encryptData(encodedSignedData);             

        // write result to output
        try {
            outStream.write(encodedEnvelopedData);
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }
    }
    
    public byte[] encryptData(byte[] content) throws NebraskaCryptoException, NebraskaFileException {
    	CMSEnvelopedDataGenerator envelopedGenerator = new CMSEnvelopedDataGenerator();              
        
    	// specify session key encryption
        AlgorithmIdentifier algorithmIdentifier;
		try {
			// with 4096 keys we use RSAES-OAEP
			if (useRSAES) {
				JcaAlgorithmParametersConverter paramsConverter = new JcaAlgorithmParametersConverter();
		        OAEPParameterSpec oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
				algorithmIdentifier = paramsConverter.getAlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP, oaepSpec);
			} else {
				algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
			}
			
		} catch (InvalidAlgorithmParameterException e) {
			throw new NebraskaCryptoException(e);
		}
        
		// define receivers
        try {
            // the receiver must be able to decrypt the data
			envelopedGenerator.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(receiverCert, algorithmIdentifier).setProvider(NebraskaConstants.SECURITY_PROVIDER));
			
			// optionally the sender may also decrypt it
			if (encryptToSelf) {
				JceKeyTransRecipientInfoGenerator recipent = new JceKeyTransRecipientInfoGenerator(senderCert,algorithmIdentifier).setProvider(NebraskaConstants.SECURITY_PROVIDER);
				envelopedGenerator.addRecipientInfoGenerator(recipent);
			}
		} catch (CertificateEncodingException e) {
			throw new NebraskaCryptoException(e);
		}
        
        CMSEnvelopedData envelopedData;
        
        byte[] encryptedContent = null;
        // build everything together and encrypt the content
		try {
			envelopedData = envelopedGenerator.generate(
					new CMSProcessableByteArray(content),                
			        new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(NebraskaConstants.SECURITY_PROVIDER).build()
			);
			
			encryptedContent  = envelopedData.getEncoded();
		} catch (CMSException e) {
			throw new NebraskaCryptoException(e);
		} catch (IOException e) {
			throw new NebraskaFileException(e);
		}            
        
        return encryptedContent;
    }
    
    public byte[] signData(byte[] signingContent)
            throws NebraskaCryptoException, NebraskaFileException {
        // generate needs a CMSProcessable
         CMSProcessableByteArray plainContent = new CMSProcessableByteArray(signingContent);

        // sign data
        ContentSigner signer;
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		try {
			JcaContentSignerBuilder signerBuilder = null;
			// with 4096 keys we use RSAES-PSS
			if (useRSAES) {
				AlgorithmParameterSpec sign_params = new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);	
				signerBuilder = new JcaContentSignerBuilder("SHA256withRSA", sign_params);
			} else {
				signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
			}
			signer = signerBuilder.setProvider(NebraskaConstants.SECURITY_PROVIDER).build(senderKey);
			generator.addSignerInfoGenerator(
					new JcaSignerInfoGeneratorBuilder(
			                new JcaDigestCalculatorProviderBuilder().setProvider(NebraskaConstants.SECURITY_PROVIDER).build())
			                .build(signer, senderCert)
			);
		} catch (OperatorCreationException | CertificateEncodingException e) {
			throw new NebraskaCryptoException(e);
		}

		CMSSignedData signedData;
        byte[] data = null;
		try {
            generator.addCertificates(certificateChain);
            signedData = generator.generate(plainContent, true);
            
            data  = signedData.getEncoded();
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        
        return data;
    }

}
