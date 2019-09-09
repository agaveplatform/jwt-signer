# Agave JWT Signer
> CLI to create a signed JWT consistent with Agave API Manager 

## 


## Generating a keystore

You can create a keystore with a self-signed key with the following command:  

```shell
keytool  -genkey \
  -alias mykey \
  -dname 'cn=my-signing-key,ou=Services,ou=tenantsandbox,dc=agaveplatform,dc=org' \
  -keyalg RSA \
  -keysize 2048 \
  -keypass changeit \
  -storepass changeit \
  -keystore example.jks   
```
### Running the tool

```

```

You can also specify a config file containing the command arguments. This is particularly helpful when running in a containerized environment as it allows you to inject the arguments in as secrets rather than expose them as environmental variables.

```

```