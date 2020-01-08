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

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_OLD = "SHA1WithRSAEncryption";
    public static final String CRQ_SIGNATURE_ALGORITHM_OLD = "SHA1withRSA";

    static final String CERTIFICATE_SIGNATURE_ALGORITHM_NEW = "SHA256WithRSAEncryption";
    public static final String CRQ_SIGNATURE_ALGORITHM_NEW = "SHA256withRSA";

    static final String FINGERPRINT_ALGORITHM = "MD5";
    static final String CERTIFICATE_TYPE = "X509";
    static final String CERTSTORE_TYPE = "Collection";

    static final int CERTIFICATE_YEARS = 3;

    // common part of X509 principal
    static final String X500_PRINCIPAL_COUNTRY = "DE";
    public static final String X500_PRINCIPAL_ORGANIZATION = "ITSG TrustCenter fuer sonstige Leistungserbringer";

    /* These variables don't belong here. */ 
    public static String CERTIFICATE_SIGNATURE_ALGORITHM_USED;
    public static String CRQ_SIGNATURE_ALGORITHM_USED;

}
