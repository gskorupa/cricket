# Cricket MSF microsite image
FROM openjdk:10-jre-slim

RUN mkdir /cricket
RUN mkdir /cricket/work
RUN mkdir /cricket/work/data
RUN mkdir /cricket/work/files
RUN mkdir /cricket/work/backup
COPY dist/cricket-1.3.0-alpha.11.jar /cricket
COPY dist/work/config/cricket.json /cricket/work/config/
COPY dist/www /cricket/www
COPY dist/work/data/cricket_publickeystore.jks /cricket/work/data
VOLUME /cricket/work
volume /cricket/www
WORKDIR /cricket

CMD ["java", "--illegal-access=deny", "--add-modules", "java.xml.bind", "--add-modules", "java.activation", "-jar", "cricket-1.3.0-alpha.11.jar", "-r", "-c", "/cricket/work/config/cricket.json", "-s", "Microsite"]