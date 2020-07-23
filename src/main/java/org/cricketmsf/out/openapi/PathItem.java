package org.cricketmsf.out.openapi;

/**
 *
 * @author greg
 */
public class PathItem {
    private Operation get;
    private Operation put;
    private Operation post;
    
    public String toYaml() {
        String indent = "    ";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        Operation operation;
        
        operation=getGet();
        if (null != operation) {
            sb.append(indent).append("get:").append(lf);
            sb.append(operation.toYaml());
        }
        return sb.toString();
    }

    /**
     * @return the get
     */
    public Operation getGet() {
        return get;
    }

    /**
     * @param get the get to set
     */
    public void setGet(Operation get) {
        this.get = get;
    }

    /**
     * @return the put
     */
    public Operation getPut() {
        return put;
    }

    /**
     * @param put the put to set
     */
    public void setPut(Operation put) {
        this.put = put;
    }

    /**
     * @return the post
     */
    public Operation getPost() {
        return post;
    }

    /**
     * @param post the post to set
     */
    public void setPost(Operation post) {
        this.post = post;
    }
}
