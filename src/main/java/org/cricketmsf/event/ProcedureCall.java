package org.cricketmsf.event;

/**
 *
 * @author greg
 */
public class ProcedureCall {

    public EventDecorator event = null;
    public String procedureName = null;
    public int responseCode = 0;
    public Object errorResponse = null;

    public ProcedureCall(EventDecorator event, String procedureName) {
        this.event = event;
        this.procedureName = procedureName;
    }

    public ProcedureCall(int responseCode, Object errorResponse) {
        this.responseCode = responseCode;
        this.errorResponse = errorResponse;
    }

}
