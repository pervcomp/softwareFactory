FROM ubuntu:18.04

MAINTAINER Ivan Krizsan, https://github.com/krizsan

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    apt-get install -y  openjdk-8-jdk && \
    apt-get install -y  zip unzip

RUN apt-get install -y git
RUN export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
VOLUME /tmp
EXPOSE 8080
EXPOSE 8090
EXPOSE 9002
ADD target/spring-softwarefactory-1.5.1.war  app.jar
ENV JAVA_OPTS=""
RUN ln -fs /usr/share/zoneinfo/Europe/Rome /etc/localtime

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
