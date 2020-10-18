package org.cricketmsf.event;

/**
 * 
 * @author greg
 */
public class ProcedureCall {

    public Event event = null;
    public String procedureName = null;
    public int responseCode = 0;
    public Object response = null;
    public String contentType = "application/json";
    public boolean requestHandled = false;

    public ProcedureCall() {
        this.responseCode = 500;
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event
     * @return
     */
    public static ProcedureCall toForward(Event event) {
        return toForward(event, event.getProcedureName(), 0);
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event
     * @param procedureName
     * @return
     */
    public ProcedureCall(Event event, String procedureName) {
        this.event = event;
        this.procedureName = procedureName;
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event
     * @param procedureName
     * @return
     */
    public static ProcedureCall toForward(Event event, String procedureName) {
        return toForward(event, procedureName, 0);
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event
     * @param procedureName
     * @param responseCode
     * @return
     */
    public static ProcedureCall toForward(Event event, String procedureName, int responseCode) {
        ProcedureCall pc = new ProcedureCall();
        pc.requestHandled = false;
        pc.event = event;
        pc.procedureName = procedureName;
        pc.responseCode = responseCode;
        return pc;
    }

    /**
     * Creates ProcedureCall object to be processed by the inbound adapter without calling the service.
     * 
     * @param responseCode
     * @param responseObject
     * @return 
     */
    public static ProcedureCall toRespond(int responseCode, Object responseObject) {
        ProcedureCall pc = new ProcedureCall();
        pc.requestHandled = true;
        pc.response = responseObject;
        pc.responseCode = responseCode;
        return pc;
    }

    /**
     * Creates ProcedureCall object to be processed by the inbound adapter without calling the service.
     * 
     * @param responseCode
     * @param contentType
     * @param responseObject
     * @return 
     */
    public static ProcedureCall toRespond(int responseCode, String contentType, Object responseObject) {
        ProcedureCall pc = new ProcedureCall();
        pc.requestHandled = true;
        pc.contentType = contentType;
        pc.response = responseObject;
        pc.responseCode = responseCode;
        return pc;
    }

}
