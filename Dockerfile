FROM maven:3-jdk-9 as maven3

# Standard Maven build of Java code to produce an executable jar
COPY ./src /app/src
COPY ./pom.xml /app/pom.xml

WORKDIR /app

RUN mvn clean
RUN mvn package
RUN ls -al target/*.jar
RUN java -jar target/agave-jwt-signer-1.0-SNAPSHOT.jar --help

FROM oracle/graalvm-ce:19.2.0 as graalvm

# Take the shaded jar and build a GraalVM native image that will run on
# any linux host without a JVM required.
RUN  gu install native-image

COPY --from=maven3 /app/target /app

RUN java -jar /app/agave-jwt-signer-1.0-SNAPSHOT.jar --help
RUN native-image \
		-jar /app/agave-jwt-signer-1.0-SNAPSHOT.jar \
	 	-H:+ReportUnsupportedElementsAtRuntime \
	 	--enable-all-security-services \
	 	--static \
	 	--no-server \
	 	/app/agave-jwt-signer-graalvm-amd64


FROM alpine:latest
MAINTAINER Rion Dooley <deardooley@gmail.com>

# Copy the executable GraalVM binary to a minimal alpine image for distribution
COPY --from=graalvm /app/agave-jwt-signer-graalvm-amd64 /bin/agave-jwt-signer-graalvm-amd64
COPY bin/agave-jwt-signer.sh bin/agave-jwt-signer
COPY ./example.jks /example.jks
COPY ./jwt.conf.example /jwt.conf.example

RUN bin/agave-jwt-signer @/jwt.conf.example

ENTRYPOINT ["/bin/agave-jwt-signer"]

CMD ['-help']