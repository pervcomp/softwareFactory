FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ADD softwareFactory/target/spring-softwarefactory-1.5.1.war  app.jar
ENV JAVA_OPTS=""
ENV TZ=Europe/Rome
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
