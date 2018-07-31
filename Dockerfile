FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV cass.contactPoint=
ENV cass.keyspace=ledger
ENV cass.username=
ENV cass.password=
ENV akka.seed.host=
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
EXPOSE 8080