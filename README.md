# Agave JWT Signer
> CLI to create a signed JWT consistent with Agave API Manager 

## Requirements
Agave Science and Admin APIs use signed JWT to validate authenticated requests coming from their API Manager gateway. In certain situations, services may need to be called security prior to being registered with the API manager. The WSO2 API Manager does not currently provide a canned way to generate a signed JWT through an externally addressable endpoint. To work around this, and to avoid insecure access patterns and backdoor vulnerabilities, this repository will generate a valid, signed JWT for use when calling services behind the WSO2 API Manager. It takes as arguments the location of a keystore with valid RSA keypair used to sign the JWT, name of the user for whom the JWT will be created, tenant code, and credentials to read the keystore and RSA key. The output will be a JWT that can be included in the appropriate `x-jwt-assertion-{{tenant_id}}` header and used to call a service.

While the code is written in Java, the resulting product will be an executable that can run on most any linux OS without the need for a JRE. We do this by packaging the executable jar created by Maven's Shade plugin as a GraalVM using the `native-image` tool. The included Dockerfile uses this approach in tandem with a multistage build to take what would otherwise be an 800MB java:9 image and instead produce a 22MB alpine image.
 

 * Docker 2018.03+

## Installing

Build the image using Docker. 

```shell
docker build --rm -t agaveplatform/agave-jwt-signer:develop .
``` 

### Getting started

The `agave-jwt-signer` command has built-in help documentation. Running the container without any arguments will print the help.

```shell
$ docker run -it --rm agaveplatform/jwt-signer:develop
Usage: agave-jwt-signer [--help] [-a=<alias>] [-k=<keypass>] [-s=<storepass>]
                        [-t=<tenantId>] [-u=<username>] FILE
Generates a signed JWT signed by a specific keystore.
      FILE                  Files whose contents to display. May also be
                              provided by setting the AGAVE_KEYSTORE_FILE
                              environment variable.
  -a, --alias=<alias>       alias of the keystore entry to use to sign the JWT.
                              May also be provided by setting the AGAVE_ALIAS
                              environment variable.
      --help                display this help and exit
  -k, --keypass=<keypass>   password of the private key referenced by 'alias'.
                              May also be provided by setting the
                              AGAVE_USERNAME environment variable.
  -s, --storepass=<storepass>
                            password of the keystore. May also be provided by
                              setting the AGAVE_STOREPASS environment variable.
  -t, --tenant=<tenantId>   Agave tenant code. May also be provided by setting
                              the AGAVE_TENANT environment variable.
  -u, --username=<username> Agave username injected into the JWT. May also be
                              provided by setting the AGAVE_USERNAME
                              environment variable. 
```

Passing passwords on the command line is generally discouraged. You can instead mount the command arguments into the container as a file and pass that to the command with the `@<path to file>` syntax. Arguments can be provided one per line or on a single line within the file.  The `jwt.conf.example` file shows an example set of arguments to generate a signed JWT using the key in the included `example.jks` keystore.

```
 docker run -it --rm -v $(pwd):/app -w /app agaveplatform/jwt-signer:develop @jwt.conf.example
```

## Generating your own keystore

You can create a keystore with a self-signed key with the following command:  

```shell
alias keytool='docker run -it --rm -v $(pwd):/app -w /app java:9 keytool'
keytool -genkey \
  -alias mykey \
  -dname 'cn=my-signing-key,ou=Services,ou=tenantsandbox,dc=agaveplatform,dc=org' \
  -keyalg RSA \
  -keysize 2048 \
  -keypass changeit \
  -storepass changeit \
  -keystore example.jks   
```

## Contributing

When you publish something open source, one of the greatest motivations is that
anyone can just jump in and start contributing to your project.

These paragraphs are meant to welcome those kind souls to feel that they are
needed. You should state something like:

"If you'd like to contribute, please fork the repository and use a feature
branch. Pull requests are warmly welcome."

If there's anything else the developer needs to know (e.g. the code style
guide), you should link it here. If there's a lot of things to take into
consideration, it is common to separate this section to its own file called
`CONTRIBUTING.md` (or similar). If so, you should say that it exists here.

## Links

- Project homepage: [https://agaveplatform.org]
- Repository: [https://github.com/agaveplatform/jwt-signer]
- Issue tracker: [https://github.com/agaveplatform/jwt-signer/issues]
  - In case of sensitive bugs like security vulnerabilities, please contact
    deardooley@gmail.com directly instead of using issue tracker. We value your effort
    to improve the security and privacy of this project!
- Related projects:
  - GraalVM: [https://www.graalvm.org]
  - JWT: https://jwt.io


## Licensing

The code in this project is licensed under BSD-3 License


