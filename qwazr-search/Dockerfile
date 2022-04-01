FROM eclipse-temurin:11-jre

MAINTAINER Emmanuel Keller

ADD target/qwazr-search-*-app.jar /usr/share/qwazr/qwazr-search.jar

VOLUME /var/lib/qwazr

EXPOSE 9091

WORKDIR /var/lib/qwazr/

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/usr/share/qwazr/qwazr-search.jar"]
