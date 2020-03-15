package org.thera_pi.nebraska.crypto;

/**
 * This class is intended to store constants with descriptive names
 * for the use inside Nebraska or by Nebraska's interface.
 * 
 * @author Bodo
 *
 */
public class NebraskaConstants {

    // constants to select algorithms and data types for BC function calls
    public static final String SECURITY_PROVIDER = "BC";
    static final String KEYSTORE_TYPE = "BCPKCS12";
    public static final String KEY_ALGORITHM = "RSA";

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_SHA1 = "SHA1WithRSAEncryption";
    public static final String CRQ_SIGNATURE_ALGORITHM_SHA1 = "SHA1withRSA";

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_SHA256 = "SHA256WithRSAEncryption";
    public static final String CRQ_SIGNATURE_ALGORITHM_SHA256 = "SHA256withRSA";

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_RSA_PSS = "SHA256WithRSAandMGF1"; 
    public static final String CRQ_SIGNATURE_ALGORITHM_RSA_PSS = "SHA256WithRSAandMGF1";
    static final String DIGEST_ENCRYPTION_ALGORITHM_RSA_PSS = "SHA256WithRSAEncryption";

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_DEFAULT = CERTIFICATE_SIGNATURE_ALGORITHM_SHA256;
    public static final String CRQ_SIGNATURE_ALGORITHM_DEFAULT = CRQ_SIGNATURE_ALGORITHM_RSA_PSS;
    static final String DIGEST_ENCRYPTION_ALGORITHM_DEFAULT = "";

    static final String FINGERPRINT_ALGORITHM = "MD5";
    static final String CERTIFICATE_TYPE = "X509";
    static final String CERTSTORE_TYPE = "Collection";

    static final int CERTIFICATE_YEARS = 3;
    static final int KEY_LENGTH = 4096;

    // common part of X509 principal
    static final String X500_PRINCIPAL_COUNTRY = "DE";
    public static final String X500_PRINCIPAL_ORGANIZATION = "ITSG TrustCenter fuer sonstige Leistungserbringer";
}
