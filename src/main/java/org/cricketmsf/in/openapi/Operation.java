package org.cricketmsf.in.openapi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author greg
 */
public class Operation {

    private String description;
    private String summary;
    private List<Parameter> parameters = new ArrayList<>();
    private List<Response> responses = new ArrayList<>();
    
    public Operation(){
        parameters=new ArrayList<>();
        responses=new ArrayList<>();
    }
    
    public Operation description(String description){
        setDescription(description);
        return this;
    }
    
    public Operation summary(String summary){
        setSummary(summary);
        return this;
    }
    
    public Operation response(Response response){
        responses.add(response);
        return this;
    }
    
    public Operation parameter(Parameter parameter){
        parameters.add(parameter);
        return this;
    }

    public String toYaml() {
        String indent = "      ";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        if (null != description) {
            sb.append(indent).append("description: \"").append(getDescription()).append("\"").append(lf);
        }
        if (null != summary) {
            sb.append(indent).append("summary: \"").append(getSummary()).append("\"").append(lf);
        }
        sb.append(indent).append("responses:").append(lf);
        responses.forEach(response->{
            sb.append(response.toYaml());
        });
        return sb.toString();
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
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the responses
     */
    public List<Response> getResponses() {
        return responses;
    }

    /**
     * @param responses the responses to set
     */
    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }
}
