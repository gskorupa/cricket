package org.cricketmsf.in.openapi;

/**
 *
 * @author greg
 */
public class Server extends Element {

    private String url = null;
    private String description = null;

    public Server(String url) {
        this.url = url;
    }

    Server description (String description) {
        this.description = description;
        return this;
    }

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("- url: '").append(this.getUrl()).append("'").append(lf);
        if (null != description) {
            sb.append(indent+indentStep).append("description: ").append(this.description).append(lf);
        }
        return sb.toString();
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
}
