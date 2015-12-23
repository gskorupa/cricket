# Cricket
Cricket is a tiny microservices framework for Java.

Please check out the development guide at GitBook: 
https://www.gitbook.com/book/gskorupa/building-microservices-with-cricket/

## Quick start

You need:
* Java 1.7
* Apache Ant
* Apache Tomcat 7 or 8 (to build servlet part of a web application)

Edit build.xml and modify properties and file paths according to your system.
Compile and build distribution package (Cricket.jar) with command:

`ant dist`

Run example service:

`java -jar cricket-1.0.0.jar --run`

The http interface will be available at http://localhost:8080/
Try

`curl -i -H "Accept:application/json" "http://localhost:8080?name=John&surname=Smith"`

To stop the service press `Ctrl-C`

*The project is not production ready so use with care. Please stay tuned for more documentation.*
