# Cricket
Tiny microservices framework.

## Quick start

You need:
* Java 1.7
* Apache Ant
* Apache Tomcat 7.x (to build or as application server for Cricket services)

Edit build.xml and modify properties and file paths according to your system
Compile and build distribution package (Cricket.jar) with command:

`ant dist`

Run example service:

`java -jar Cricket.jar`

The http interface should be available at http://localhost:8080/

`curl -i -H "Accept:application/json" "http://localhost:8080?name=John&surname=Smith"`

To stop the service press `Ctrl-C`

*The project is in initial phase. Please stay tuned for more documentation.*
