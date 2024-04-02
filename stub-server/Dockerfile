ARG JAVA_VERSION=11
FROM openjdk:$JAVA_VERSION-jdk-slim

ENV GROUP net.grandcentrix.appscaffold
ENV NAME client-api
ENV VERSION 0.0.1-SNAPSHOT

# Download Runner
ARG CLOUD_CONTRACT_VERSION=2.2.2.RELEASE
ADD https://search.maven.org/remotecontent?filepath=org/springframework/cloud/spring-cloud-contract-stub-runner-boot/\
$CLOUD_CONTRACT_VERSION/spring-cloud-contract-stub-runner-boot-$CLOUD_CONTRACT_VERSION.jar \
/app/stub-runner.jar

# Copy Contracts
COPY v1/ /app/contracts/v1/
# Delete GZip Files, due to an error which tries to include these files
RUN find /app/contracts/v1/ -type f -name '*.gzip' -delete

WORKDIR /app
EXPOSE 8081
CMD [ "java", "-cp", "stub-runner.jar", \
    "-Djava.security-egd=/dev/./urandom", \
    "-Dstubrunner.generate-stubs=true", \
    "-Dstubrunner.ids=$GROUP:$NAME:stubs:8081", \
    "-Dstubrunner.stubs-mode=REMOTE", \
    "-Dstubrunner.repositoryRoot=stubs://file:///app/contracts", \
    "org.springframework.boot.loader.JarLauncher" \
]