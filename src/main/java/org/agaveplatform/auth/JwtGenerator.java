package org.agaveplatform.auth;

import com.nimbusds.jwt.JWT;
import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.text.Normalizer;
import java.util.concurrent.Callable;

@Command(name="agave-jwt-signer", description = "Generates a signed JWT signed by a specific keystore.")
public class JwtGenerator implements Callable<Integer> {

    File keystoreFile;
    String username;
    String tenantId;
    String alias;
    String storepass;
    String keypass;

    @Option(names = "--help", usageHelp = true, description = "display this help and exit")
    boolean help;

    @Spec
    CommandSpec spec;

    @Parameters(paramLabel = "FILE", arity = "1",
            description = "Files whose contents to display. May also be provided by setting the AGAVE_KEYSTORE_FILE" +
                    " environment variable.")
    public void setKeystoreFile(File keystoreFile) {
        if (keystoreFile == null) {
            String keystorePath = System.getenv("AGAVE_KEYSTORE_FILE");
            if (keystorePath == null || keystorePath.equals("") ) {
                throw new ParameterException(spec.commandLine(), "location of keystore file is required");
            } else {
                this.keystoreFile = new File(keystorePath);
            }
        }
        else {
            this.keystoreFile = keystoreFile;
        }
    }

    @Option(names = {"-u", "--username"},
            description = "Agave username injected into the JWT. May also be provided by setting the AGAVE_USERNAME environment variable.")
    public void setUsername(String username) {
        this.username = (username == null ? System.getenv("AGAVE_USERNAME") : username);

        if (this.username == null || this.username.equals("") ) {
            throw new ParameterException(spec.commandLine(), "Agave username required.");
        }
    }

    @Option(names = {"-t", "--tenant"},
            description = "Agave tenant code. May also be provided by setting the AGAVE_TENANT " +
                    "environment variable.")
    public void setTenantId(String tenantId) {
        this.tenantId = (tenantId == null ? System.getenv("AGAVE_TENANT") : tenantId);

        if (this.tenantId == null || this.tenantId.equals("") ) {
            throw new ParameterException(spec.commandLine(), "Agave tenant code required");
        } else {
            this.tenantId = Normalizer.normalize(this.tenantId, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^\\w+]", "_")
                    .replaceAll("\\s+", "_")
                    .replaceAll("[-]+", "_")
                    .replaceAll("^-", "")
                    .replaceAll("-$", "").toUpperCase();
        }
    }

    @Option(names = {"-a", "--alias"},
            description = "alias of the keystore entry to use to sign the JWT. " +
                    "May also be provided by setting the AGAVE_ALIAS environment variable.")
    public void setAlias(String alias) {
        this.alias = (alias == null ? System.getenv("AGAVE_ALIAS") : alias);

        if (this.alias == null || this.alias.equals("") ) {
            throw new ParameterException(spec.commandLine(), "keystore alias required");
        }
    }

    @Option(names = {"-s", "--storepass"},
            description = "password of the keystore. May also be provided by setting the AGAVE_STOREPASS " +
                    "environment variable.")
    public void setStorepass(String storepass) {
        this.storepass = (storepass == null ? System.getenv("AGAVE_STOREPASS") : storepass);

        if (this.storepass == null || this.storepass.equals("") ) {
            throw new ParameterException(spec.commandLine(), "keystore password required");
        }
    }

    @Option(names = {"-k", "--keypass"},
            description = "password of the private key referenced by 'alias'. May also be provided by setting " +
                    "the AGAVE_USERNAME environment variable.")
    public void setKeypass(String keypass) {
        this.keypass = keypass;
        this.keypass = (keypass == null || keypass.equals("") ? System.getenv("AGAVE_KEYPASS") : keypass);

        if (this.keypass == null || this.keypass.equals("") ) {
            throw new ParameterException(spec.commandLine(), "private key password required");
        }
    }

    @Override
    public Integer call() throws Exception {
        try {
            KeystoreManager keystoreManager = KeystoreManager.getInstance(keystoreFile, storepass.toCharArray());

            AgaveJWT agaveJwt = new AgaveJWT(this.tenantId,
                    keystoreManager.getPrivateKey(this.alias, this.keypass.toCharArray()),
                    keystoreManager.getCertificate(this.alias));

            JWT jwt = agaveJwt.getSignedJwtForUser(this.username);

            if (jwt == null) {
                throw new picocli.CommandLine.ExecutionException(spec.commandLine(), "Unable to generate signed JWT for " + keystoreFile.getAbsolutePath());
            } else {
                System.out.println(jwt.serialize());
                return 0;
            }
        } catch (picocli.CommandLine.ExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new picocli.CommandLine.ExecutionException(spec.commandLine(), e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new JwtGenerator()).execute(args);
        System.exit(exitCode);
    }
}
