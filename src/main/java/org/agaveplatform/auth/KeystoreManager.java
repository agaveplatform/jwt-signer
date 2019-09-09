package org.agaveplatform.auth;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class KeystoreManager {

    private String path;
    private KeyStore keystore;
    private static KeystoreManager km;

    public KeystoreManager() {}

    public static KeystoreManager getInstance(File keystoreFile, char[] keystorePassword) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        if (km == null) {
            km = new KeystoreManager();
            km.readKeystore(keystoreFile, keystorePassword);
        }

        return km;
    }

    /**
     * Loads and parses a keystore on the local disk into memory.
     * @param keystoreFile keystore file on disk
     * @param keystorePassword password to the keystore
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    protected void readKeystore(File keystoreFile, char[] keystorePassword) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

        byte[] bytes = Files.readAllBytes(keystoreFile.toPath());

        this.keystore = KeyStore.getInstance("JKS");

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        this.keystore.load(stream, keystorePassword);//KS_PASS.toCharArray());
    }

    /**
     * Fetches the public key certificate for the given alias from the current keystore
     *
     * @param alias the alias to the current keystore
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    protected Certificate getCertificate(String alias) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return this.keystore.getCertificate(alias);
    }


    /**
     * Fetches the private key for the given alias from the current keystore.
     * @param alias key alias
     * @param keyPassword password to the private key
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    protected PrivateKey getPrivateKey(String alias, char[] keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return (PrivateKey) keystore.getKey(alias, keyPassword);//KS_PASS.toCharArray());
    }
}
