FROM curlimages/curl:7.87.0 as cache

ARG MAVEN_SETTINGS_URL
# heredoc beware of the CR/LF
RUN <<EOF
# if MAVEN_SETTINGS_URL is set, download the settings.xml file
mkdir -p /home/curl_user/config
if [ -n "$MAVEN_SETTINGS_URL" ]; then
    echo "Downloading settings.xml from $MAVEN_SETTINGS_URL"
    curl -s -L -o /home/curl_user/config/settings.xml $MAVEN_SETTINGS_URL
fi
EOF

FROM maven:3.8.1-openjdk-17 as builder
# copy the config file from the previous stage to tmp
COPY --from=cache /home/curl_user/config /tmp/
RUN <<EOF
# if the settings.xml file exists, copy it to the maven config directory
if [ -f "/tmp/settings.xml" ]; then
    echo "Copying settings.xml to /usr/share/maven/ref/"
    cp /tmp/settings.xml /usr/share/maven/ref/settings.xml
    # the link of the ref directory is during the entrypoint of the maven image therefore is not available at build time and we need to link it manually
    mkdir -p /root/.m2
    ln -s /usr/share/maven/ref/settings.xml /root/.m2/settings.xml
fi
EOF

WORKDIR /doml
COPY git/doml-concepts/implementation/.mvn /doml/.mvn
COPY git/doml-concepts/implementation/pom.xml /doml/pom.xml
COPY git/doml-concepts/implementation/eu.piacere.doml/META-INF /doml/eu.piacere.doml/META-INF
COPY git/doml-concepts/implementation/eu.piacere.doml.grammar/META-INF /doml/eu.piacere.doml.grammar/META-INF
RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -B

WORKDIR /code
COPY pom.xml /code/pom.xml
RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -B

WORKDIR /doml
COPY git/doml-concepts/implementation/ /doml/.
# clean has sense if we are developing locally and testing with mvn
RUN mvn clean install -Dmaven.test.skip=true

WORKDIR /code
COPY src /code/src
COPY openapi.yaml .openapi-generator-ignore /code/

RUN mvn package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.test.skip=true \
    && mv target/eu.piacere.d2x.server-*.jar /app.jar

FROM openjdk:17-slim AS prod

WORKDIR /root

COPY --from=builder /app.jar /app.jar

EXPOSE 8080
CMD ["java","-jar","/app.jar"]
