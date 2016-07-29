package au.com.mountainpass.hyperstate.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import au.com.mountainpass.hyperstate.SelfSignedCertificate;

@Configuration
public class HyperstateClientCertConfig {

    @Value("${server.ssl.key-alias}")
    String keyAlias;

    @Value("${javax.net.ssl.trustStore:build/truststore.jks}")
    private String trustStoreFile;

    @Value("${javax.net.ssl.trustStorePassword:changeit}")
    private String trustStorePassword;

    @Value("${javax.net.ssl.trustStoreType:JKS}")
    private String trustStoreType;

    @Autowired
    SelfSignedCertificate selfSignedCertificate;

    public String systemDefaultTrustStoreLocation() {
        final String javaHome = System.getProperty("java.home");
        final FileSystemResource location = new FileSystemResource(
                javaHome + "/lib/security/jssecacerts");
        if (location.exists()) {
            return location.getFilename();
        } else {
            return javaHome + "/lib/security/cacerts";
        }
    }

    public String getTrustStoreLocation() {
        if (StringUtils.hasLength(trustStoreFile)) {
            return trustStoreFile;
        }
        final String locationProperty = System
                .getProperty("javax.net.ssl.trustStore");
        if (StringUtils.hasLength(locationProperty)) {
            return locationProperty;
        } else {
            return systemDefaultTrustStoreLocation();
        }
    }

    @Bean
    KeyStore trustStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException {

        String trustStoreLocation = getTrustStoreLocation();
        SelfSignedCertificate.addCertToTrustStore(trustStoreLocation,
                trustStorePassword, trustStoreType, keyAlias,
                selfSignedCertificate);

        final KeyStore ks = KeyStore.getInstance(trustStoreType);

        final File trustFile = new File(trustStoreLocation);
        ks.load(new FileInputStream(trustFile),
                trustStorePassword.toCharArray());
        return ks;
    }

}
