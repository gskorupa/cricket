package org.cricketmsf.in.openapi;

/**
 *
 * @author greg
 */
public class Info extends Element {

    private String title = null;
    private String description = null;
    private String termsOfService = null;
    private String version=null;
    
    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        if(null!=getVersion()){
            sb.append(indent).append("version: '").append(this.getVersion()).append("'").append(lf);
        }
        if(null!=getTitle()){
            sb.append(indent).append("title: '").append(this.getTitle()).append("'").append(lf);
        }
        if(null!=getDescription()){
            sb.append(indent).append("description: '").append(this.getDescription()).append("'").append(lf);
        }
        if(null!=getTermsOfService()){
            sb.append(indent).append("termsOfService: '").append(this.getTermsOfService()).append("'").append(lf);
        }
        return sb.toString();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
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

    /**
     * @return the termsOfService
     */
    public String getTermsOfService() {
        return termsOfService;
    }

    /**
     * @param termsOfService the termsOfService to set
     */
    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
