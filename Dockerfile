FROM java:openjdk-8-jdk-alpine

MAINTAINER Emmanuel Keller

ADD http://download.qwazr.com/latest/qwazr-search-1.0.1-SNAPSHOT-exec.jar /usr/share/qwazr/qwazr-search.jar

VOLUME /var/lib/qwazr

EXPOSE 9091

WORKDIR /var/lib/qwazr/

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/usr/share/qwazr/qwazr-search.jar"]
