# Cricket Microservices Framework for Java.

## Quick start

You need:
* Java 1.8
* Apache Ant

Edit build.xml to modify properties and file paths according to your system. 
Modify properties in cricket.json.
Compile and build distribution package (Cricket.jar) with command:

`ant dist`

Run example service:

`java -jar cricket-1.0.0.jar --run`

The http interface will be available at `http://localhost:8080/` To request the build in example service you can use curl:

`curl -i -H "Accept:application/json" "http://localhost:8080?name=John&surname=Smith"`

To stop the service press `Ctrl-C`

## Building a service with Cricket

Use the service template as the starting point. Clone https://github.com/gskorupa/cricket-starter

## Documentation

For project status and the roadmap look at https://github.com/gskorupa/Cricket/wiki

Development guide (preview): https://www.gitbook.com/book/gskorupa/building-microservices-with-cricket/

Additional information: http://gskorupa.blogspot.com/

*The project is under development. Please stay tuned for changes and more documentation.*
