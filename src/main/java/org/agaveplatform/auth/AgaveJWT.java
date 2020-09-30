package org.agaveplatform.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class AgaveJWT {

    private final String API_GATEWAY_ID = "http://wso2.org/claims";

    private static final Base64 base64Url = new Base64();

    private final Map<String, Object> claims = new LinkedHashMap<>();

    private String tenantId;

    private Certificate certificate;

    private PrivateKey privateKey;

    public AgaveJWT(String tenantId, PrivateKey privateKey, Certificate certificate) {
        this.tenantId = tenantId;
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public JWT getSignedJwtForUser(String username) throws Exception {
        //generating expiring timestamp
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireIn = currentTime + 1000 * 60 * 4;
        String tenantDomain = "sandbox.agaveplatform.org";
        int tenantCode = -1234;

        // Prepare JWT with claims set

        String[] roles = new String[]{
                "Internal/" + tenantId + "-services-admin",
                "Internal/" + tenantId + "-tenant-admin",
                "Internal/" + tenantId + "-user-account-manager",
                "Internal/" + tenantId + "-impersonator",
                "Internal/everyone",
                "Internal/" + tenantId + "_" + username + "_DefaultApplication_PRODUCTION"
        };

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("wso2.org/products/am")
                .subject(username)
                .issueTime(new Date())
                .expirationTime(new Date(expireIn))
                .claim(API_GATEWAY_ID + "/subscriber", "")
                .claim(API_GATEWAY_ID + "/applicationid", "1")
                .claim(API_GATEWAY_ID + "/applicationname", "DefaultApplication")
                .claim(API_GATEWAY_ID + "/applicationtier", "Unlimited")
                .claim(API_GATEWAY_ID + "/apicontext", "/admin")
                .claim(API_GATEWAY_ID + "/version", "2.0")
                .claim(API_GATEWAY_ID + "/tier", "Unlimited")
                .claim(API_GATEWAY_ID + "/keytype", "PRODUCTION")
                .claim(API_GATEWAY_ID + "/usertype", "APPLICATION_USER")
                .claim(API_GATEWAY_ID + "/enduser", username)
                .claim(API_GATEWAY_ID + "/enduserTenantId", "-1234")
                .claim(API_GATEWAY_ID + "/emailaddress", username + "@example.com")
                .claim(API_GATEWAY_ID + "/fullname", username)
                .claim(API_GATEWAY_ID + "/givenname", "")
                .claim(API_GATEWAY_ID + "/lastname", "")
                .claim(API_GATEWAY_ID + "/role", StringUtils.join(roles, ','))
                .claim(API_GATEWAY_ID + "/title", "N/A")
                .build();


        Base64URL base64URL = new Base64URL(getThumbPrint(this.certificate, tenantDomain));
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .x509CertSHA256Thumbprint(base64URL)
                .build();
        SignedJWT jwt = new SignedJWT(header, claimsSet);
        return signJWTWithRSA((SignedJWT) jwt, this.privateKey);
    }

    /**
     * Sign with given RSA Algorithm
     *
     * @param signedJWT
     * @param privateKey
     * @return
     * @throws Exception
     */
    private SignedJWT signJWTWithRSA(SignedJWT signedJWT, PrivateKey privateKey)
            throws Exception {

        try {
            JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey, true);
            signedJWT.sign(signer);
            return signedJWT;
        } catch (JOSEException e) {
            throw new Exception("Error in obtaining tenant's keystore", e);
        }
    }

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param certificate
     * @param tenantDomain
     */
    private String getThumbPrint(Certificate certificate, String tenantDomain) throws Exception {

        try {

            // TODO: maintain a hashmap with tenants' pubkey thumbprints after first initialization

            //generate the SHA-1 thumbprint of the certificate
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            byte[] der = certificate.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();

            String publicCertThumbprint = hexify(digestInBytes);
            String base64EncodedThumbPrint = new String(base64Url.encode(publicCertThumbprint.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
            return base64EncodedThumbPrint;

        } catch (Exception e) {
            String error = "Error in obtaining certificate for tenant " + tenantDomain;
            throw new Exception(error, e);
        }
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

}
