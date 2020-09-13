package org.cricketmsf.in.openapi;

/**
 *
 * @author greg
 */
public class BodyContent {

    private String name;
    private Schema schema;

    public BodyContent(String name, Schema schema) {
        this.name = name;
        this.schema = schema;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }
}
