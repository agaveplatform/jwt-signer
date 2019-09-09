FROM maven:3-jdk-9

ADD . /usr/src/app
WORKDIR /usr/src/app
RUN mvn package

FROM java:9-alpine

WORKDIR /usr/src/app
COPY --from=0 /usr/src/app/target/agave-jwt-signer-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "/usr/src/app/target/agave-jwt-signer-1.0-SNAPSHOT.jar"]
CMD ["-h"]