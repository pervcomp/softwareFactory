FROM maven:3-jdk-8 as build
MAINTAINER Jukka-Pekka Venttola, https://github.com/venttola

WORKDIR /build 
COPY . /build

RUN mvn install 

FROM openjdk:8-jre-alpine

RUN apk update && apk add \
  git 

RUN addgroup -S webapp && \
    adduser -S -h /app -G webapp webapp

EXPOSE 8080
EXPOSE 8090
EXPOSE 9002
VOLUME /tmp
ENV JAVA_OPTS=""
RUN ln -fs /usr/share/zoneinfo/Europe/Rome /etc/localtime
COPY --from=build /build/target/webapp-1.5.1.war /app/app.jar


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]
