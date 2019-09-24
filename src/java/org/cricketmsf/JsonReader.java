package org.cricketmsf;

public class JsonReader {

    public static Object jsonToJava(String source, Class cls) {
        String jsonString = source;
        jsonString = "{\"@type\":\"" + cls.getName() + "\","
                + jsonString.substring(jsonString.indexOf("{") + 1);
        return com.cedarsoftware.util.io.JsonReader.jsonToJava(jsonString);
    }
    
    public static Object jsonToJava(String source) {
        return com.cedarsoftware.util.io.JsonReader.jsonToJava(source);
    }

}
