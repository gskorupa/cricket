package org.cricketmsf.in.openapi;

import java.util.ArrayList;

/**
 *
 * @author greg
 */
public class Response {

    String codeName;
    private String description;
    private ArrayList<Content> content;

    public Response(String codeName) {
        this.codeName = codeName;
        description = "";
        content = new ArrayList<>();
    }

    public Response description(String description) {
        this.description = description;
        return this;
    }

    public Response content(Content content) {
        this.content.add(content);
        return this;
    }

    public Response content(String content) {
        this.content.add(new Content(content));
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String toYaml() {
        String indent = "        ";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(codeName).append(":").append(lf);
        if (null != description) {
            sb.append(indent + "  ").append("description: '").append(getDescription()).append("'").append(lf);
        }
        if (content.size() > 0) {
            sb.append(indent + "  ").append("content:").append(lf);
            content.forEach(cnt -> {
                sb.append(indent + "    ").append("'").append(cnt.getName()).append("':").append(" {}").append(lf);
            });
        }

        return sb.toString();
    }

}
