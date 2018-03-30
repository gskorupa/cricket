# Cricket Microservices Framework for Java.

## Quick start

Get latest Cricket distribution from [GitHub](https://github.com/gskorupa/Cricket/releases):

    wget https://github.com/gskorupa/Cricket/releases/download/1.2.38/cricket-1.2.38.jar

Run default service:

    java -jar cricket-1.2.38.jar -r

Test built-in echo API using web browser or cURL:

    curl -i http://localhost:8080/api/echo 
    curl -i http://localhost:8080/api/echo?myparam=abcd

You can also try built-in webserver by creating files in www subfolder (this is preconfigured location):

    mkdir www
    echo "Hello World" > www/index.html
    java -jar cricket-1.2.38.jar -r
    curl http://localhost:8080

## Rapid prototyping of microservices

Cricket MSF can be used for rapid prototyping of microservices by giving basic 
building blocks to developers so they can focus on required business logic. 
This approach can be used eg. in the design phase to check the solution concept.

As an example lets build simple Hello World service by extending built-in BasicService. 
What we need to to is overriding getAdapters() method and to provide minimum one 
handler method with expected logic.

    // MyService.java
    import java.util.HashMap;
    import java.util.ArrayList;
    import org.cricketmsf.Event;
    import org.cricketmsf.annotation.HttpAdapterHook;
    import org.cricketmsf.in.http.StandardHttpAdapter;
    import org.cricketmsf.in.http.StandardResult;
    import org.cricketmsf.services.BasicService;
    
    public class MyService extends BasicService {
    
        StandardHttpAdapter myAdapter = null;
        
        @Override
        public void getAdapters() {
            super.getAdapters();      
            myAdapter = new StandardHttpAdapter();
            myAdapter.loadProperties(new HashMap<>(), "myadapter");
            myAdapter.setContext("/test");
            registerAdapter("myadapter", myAdapter);
        }
        
        @HttpAdapterHook(adapterName = "myadapter", requestMethod = "GET")
        @Override
        public Object doGetScript(Event requestEvent) {
            ArrayList al = new ArrayList();
            al.add("Hello");
            al.add("World");
            StandardResult r = new StandardResult();
            r.setData(al);
            return r;
        }    
    }

After compiling the class:

    javac -classpath cricket-1.2.38.jar MyService.java

We can run the service using Cricket's "lift" mode:

    java -cp .:cricket-1.2.38.jar org.cricketmsf.Runner -r -l MyService

Our service will be accessible on localhost so we can request it on it's defined 
context "/test":

    curl -i http://localhost:8080/test
    
    HTTP/1.1 200 OK
    Pragma: no-cache
    Date: Thu, 16 Mar 2017 12:31:17 GMT
    Last-modified: Cz, 16 mar 2017 13:31:17 CET
    Content-type: application/json; charset=UTF-8
    Content-length: 26
    
    [
    "Hello",
    "World"
    ]

## Fully functional microservices

To see how to build a fully functional microservice, you can start by studying 
org.cricketmsf.services.Microsite and org.cricketmsf.services.BasicService source code. 
Then read the available documentation starting from https://cricketmsf.org

## Cricket development quick start
To build Cricket you will need:
* Java 1.8
* Apache Ant

Edit build.xml to modify properties and file paths according to your system. 

Compile and build distribution packages with command:

    ant distribution


## More information

Go to https://cricketmsf.org to find more information about the platform.

In case of problems or questions please create issue in the project Github repository.

