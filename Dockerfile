# Cricket MSF microsite image
FROM gskorupa/jdk-9-ea-alpine

RUN mkdir /usr/cricket
COPY dist/cricket-1.2.33.jar /usr/cricket
RUN mkdir /usr/cricket/work
RUN mkdir /usr/cricket/work/data
COPY dist/config/cricket.json /usr/cricket/work/config/
COPY www /usr/cricket/work/www
#COPY dist/data/cricket_publickeystore.jks /usr/cricket/work/data
RUN mkdir /usr/cricket/work/files
RUN mkdir /usr/cricket/work/backup
VOLUME /usr/cricket/work
WORKDIR /usr/cricket
#CMD ["java", "-jar", "./cricket-1.2.33.jar", "-r", "-c", "work/config/cricket.json"]
CMD ["java", "-jar", "./cricket-1.2.33.jar", "-r"]