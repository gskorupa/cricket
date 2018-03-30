# Cricket MSF microsite image

#FROM java:8
#FROM gskorupa/jdk-9-ea-alpine
FROM openjdk:10-jdk

RUN mkdir /usr/cricket
RUN mkdir /usr/cricket/work
RUN mkdir /usr/cricket/work/data
RUN mkdir /usr/cricket/work/files
RUN mkdir /usr/cricket/work/backup
COPY dist/cricket-1.2.38.jar /usr/cricket
COPY dist/work/config/cricket.json /usr/cricket/work/config/
COPY dist/work/www /usr/cricket/work/www
COPY dist/work/data/cricket_publickeystore.jks /usr/cricket/work/data
VOLUME /usr/cricket/work
WORKDIR /usr/cricket

#CMD ["java", "-jar", "./cricket-1.2.38.jar", "-r", "-c", "work/config/cricket.json"]
#CMD ["java", "-jar", "./cricket-1.2.38.jar", "-r"]
CMD ["java", "--add-modules", "java.activation", "-jar", "cricket-1.2.38.jar", "-r", "-c", "/usr/cricket/work/config/cricket.json", "-s", "Microsite"]