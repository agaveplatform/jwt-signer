FROM maven:3-jdk-9

# Standard Maven build of Java code to produce an executable jar
ADD src pom.xml /app/
WORKDIR /app
RUN mvn package


FROM oracle/graalvm-ce:19.2.0

# Take the shaded jar and build a GraalVM native image that will run on
# any linux host without a JVM required.
RUN  gu install native-image

COPY --from=0 /app/target/agave-jwt-signer-1.0-SNAPSHOT-shaded.jar /app/

RUN native-image \
		-jar /app/agave-jwt-signer-1.0-SNAPSHOT-shaded.jar \
	 	-H:+ReportUnsupportedElementsAtRuntime \
	 	--enable-all-security-services \
	 	--static \
	 	--no-server \
	 	/app/agave-jwt-signer-graalvm-amd64


FROM alpine:latest
MAINTAINER Rion Dooley <deardooley@gmail.com>

# Copy the executable GraalVM binary to a minimal alpine image for distribution
COPY --from=1 /app/agave-jwt-signer-graalvm-amd64 /bin/agave-jwt-signer-graalvm-amd64
COPY bin/agave-jwt-signer.sh bin/agave-jwt-signer

ENTRYPOINT ["/bin/agave-jwt-signer"]

CMD ['-help']