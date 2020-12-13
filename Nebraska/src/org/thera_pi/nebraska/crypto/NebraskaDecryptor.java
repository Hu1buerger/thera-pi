package org.thera_pi.nebraska.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * Class to decrypt encrypted data and check the signature.
 * Only used for testing. The data exchange procedure defines a one-way
 * communication only. 
 * The decrypt function will try to decrypt an InputStream to an 
 * OutputStream and check the signature. This is only possible if the
 * data was encrypted (also) using my own public key. If the own 
 * key is not part of the recipients, it will not write any data to
 * the output.
 * 
 * General use case:
 * - Get a NebraskaDecryptor from the NebraskaKeystore object.
 * - Use the decrypt function to decrypt and check the data.
 *
 * @author Bodo
 *
 */
public class NebraskaDecryptor {

    private X509Certificate certificate;
    private PrivateKey privateKey;
    private String issuer;
    private BigInteger serial;
    
    /**
     * Create a Nebraska decryptor for self
     * 
     * @param nebraskaKeystore reference to NebraskaKeystore object that contains
     *                         the key store
     * @throws NebraskaCryptoException
     * @throws NebraskaNotInitializedException
     */
    NebraskaDecryptor(NebraskaKeystore nebraskaKeystore)
            throws NebraskaCryptoException, NebraskaNotInitializedException {
        certificate = nebraskaKeystore.getSenderCertificate();
        privateKey = nebraskaKeystore.getSenderKey();
        issuer = certificate.getIssuerDN()
                            .getName();
        serial = certificate.getSerialNumber();
    }

    /**
     * Decrypt data and check signature.
     * 
     * @param inStream  encrypted data stream
     * @param outStream plain text data stream 
     * @throws NebraskaFileException
     * @throws NebraskaCryptoException
     */
    public void decrypt(InputStream inStream, OutputStream outStream)
            throws NebraskaCryptoException, NebraskaFileException {
        CMSEnvelopedDataParser parser;
        try {
            parser = new CMSEnvelopedDataParser(inStream);
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }
        RecipientInformationStore recipients = parser.getRecipientInfos();

        Collection<?> c = recipients.getRecipients();
        Iterator<?> it = c.iterator();

        NebraskaPrincipal myPrincipal = new NebraskaPrincipal(this.issuer);
        while (it.hasNext()) {
            RecipientInformation recipient = (RecipientInformation) it.next();
            KeyTransRecipientId rid = (KeyTransRecipientId)recipient.getRID();

            BigInteger serial = rid.getSerialNumber();

            NebraskaPrincipal receiverPrincipal = new NebraskaPrincipal(issuer);
            if (myPrincipal.equals(receiverPrincipal) && this.serial.equals(serial)) {
                CMSTypedStream recData = null;
                try {
                    recData = recipient.getContentStream(new JceKeyTransEnvelopedRecipient(privateKey).setProvider(NebraskaConstants.SECURITY_PROVIDER));
                } catch (CMSException e) {
                    throw new NebraskaCryptoException(e);
                } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                processSignedData(recData.getContentStream(), outStream);
                break;
            }
        }
    }

    /**
     * Process the signed data stream created by the decryption step and check the
     * validity of the signature.
     * 
     * @param signedContentStream signed data stream
     * @param outStream           stream to write the plain data to
     * @throws NebraskaCryptoException
     * @throws NebraskaFileException
     */
    public void processSignedData(InputStream signedContentStream, OutputStream outStream)
            throws NebraskaCryptoException, NebraskaFileException {
        CMSSignedDataParser parser = null;
        try {
            parser = new CMSSignedDataParser(new JcaDigestCalculatorProviderBuilder().setProvider(NebraskaConstants.SECURITY_PROVIDER).build(), signedContentStream);
        } catch (CMSException e) {
            throw new NebraskaCryptoException(e);
        } catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        CMSTypedStream signedContent = parser.getSignedContent();

        InputStream contentStream = signedContent.getContentStream();

        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = contentStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, len);
            }
            outStream.flush();

        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }

        try {
            signedContent.drain();
        } catch (IOException e) {
            throw new NebraskaFileException(e);
        }

        Store<?> certStore;
		try {
			certStore = parser.getCertificates();
		} catch (CMSException e) {
			throw new NebraskaCryptoException(e);
		}
		
        SignerInformationStore signers;
		try {
			signers = parser.getSignerInfos();
		} catch (CMSException e) {
			throw new NebraskaCryptoException(e);
		}
        
        Collection<SignerInformation>              c = signers.getSigners();
        Iterator<SignerInformation>                it = c.iterator();

        while (it.hasNext()) {
        	SignerInformation   signer = (SignerInformation)it.next();
        	Collection<?>          certCollection = certStore.getMatches(signer.getSID());
            
        	Iterator<?>        certIt = certCollection.iterator();
        	X509CertificateHolder cert = (X509CertificateHolder)certIt.next();

            boolean verified = false;
            try {
				verified = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(NebraskaConstants.SECURITY_PROVIDER).build(cert));
            } catch (CertificateException e) {
            	throw new NebraskaCryptoException(e);
            } catch (OperatorCreationException e) {
            	throw new NebraskaCryptoException(e);
            } catch (CMSException e) {
                throw new NebraskaCryptoException(e);
            }
            if (!verified) {
                throw new NebraskaCryptoException(new Exception("signature verification failed"));
            }
        }

    }

}
