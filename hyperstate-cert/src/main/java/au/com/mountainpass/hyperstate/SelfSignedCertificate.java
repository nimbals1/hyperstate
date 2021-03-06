package au.com.mountainpass.hyperstate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;

public class SelfSignedCertificate {

    static {
        // adds the Bouncy castle provider to java security
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyPair keyPair;
    private X509Certificate cert;

    public SelfSignedCertificate(String domainName) throws Exception {
        this.keyPair = createKeyPair();
        this.cert = createSelfSignedCertificate(keyPair, domainName);
    }

    private X509Certificate createSelfSignedCertificate(final KeyPair keyPair,
            final String domainName) throws Exception {
        // generate a key pair

        // see
        // http://www.bouncycastle.org/wiki/display/JA1/X.509+Public+Key+Certificate+and+Certification+Request+Generation

        final Date startDate = new Date();
        final Date expiryDate = new Date(
                System.currentTimeMillis() + (1000L * 60 * 60 * 24));
        final BigInteger serialNumber = BigInteger
                .valueOf(Math.abs((long) (new SecureRandom().nextInt()))); // serial
        // number for
        // certificate

        final X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        final X500Principal dnName = new X500Principal("CN=" + domainName);
        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(dnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(dnName); // note: same as issuer
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        final X509Certificate cert = certGen.generate(keyPair.getPrivate(),
                "BC");

        return cert;
    }

    private KeyPair createKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator
                .getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public X509Certificate getCertificate() {
        return this.cert;
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    static public void addCertToTrustStore(final String trustStoreFile,
            final String trustStorePassword, final String trustStoreType,
            final String keyAlias,
            final SelfSignedCertificate selfSignedCertificate)
                    throws KeyStoreException, IOException,
                    NoSuchAlgorithmException, CertificateException {
        if (trustStoreFile != null) {
            final KeyStore ks = KeyStore.getInstance(trustStoreType);
            final File trustFile = new File(trustStoreFile);
            ks.load(null, null);
            ks.setCertificateEntry(keyAlias,
                    selfSignedCertificate.getCertificate());
            final FileOutputStream fos = new FileOutputStream(trustFile);
            ks.store(fos, trustStorePassword.toCharArray());
            fos.close();
        }
    }

    public static void addPrivateKeyToKeyStore(String keyStore,
            String keyStorePassword, String keyPassword, String keyAlias,
            SelfSignedCertificate selfSignedCertificate)
                    throws KeyStoreException, NoSuchAlgorithmException,
                    CertificateException, IOException {
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // load an empty key store
        ks.load(null, keyStorePassword.toCharArray());

        // add the key
        ks.setKeyEntry(keyAlias, selfSignedCertificate.getPrivateKey(),
                keyPassword.toCharArray(),
                new java.security.cert.Certificate[] {
                        selfSignedCertificate.getCertificate() });
        // Write the key store to disk.
        File ksFile = new File(keyStore);
        ksFile.getParentFile().mkdirs();
        final FileOutputStream fos = new FileOutputStream(ksFile);
        ks.store(fos, keyStorePassword.toCharArray());
        fos.close();
    }
}
