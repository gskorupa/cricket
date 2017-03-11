# Cricket Microservices Framework for Java.

## Quick start

To build you need:
* Java 1.8
* Apache Ant

Edit build.xml to modify properties and file paths according to your system. 
Modify properties in cricket.json.
Compile and build distribution package (Cricket.jar) with command:

`ant dist`

Run example service:

`java -jar cricket-version-number.jar --run`

The http interface will be available at `http://localhost:8080/` To request the build in example service you can use curl:

`curl -i -H "Accept:application/json" "http://localhost:8080?name=John&surname=Smith"`

To stop the service press `Ctrl-C`

## Building a services with Cricket

It's highly recommended to use the service template as a starting point: https://github.com/gskorupa/cricket-starter

## Documentation

Additional information: http://gskorupa.blogspot.com/


*The project is under development. Please stay tuned for changes and more documentation.*
