package org.cricketmsf.event;

/**
 * 
 * @author greg
 */
public class ProcedureCall {

    public Event event = null;
    public int responseCode = 0;
    public Object response = null;
    public String contentType = "application/json";
    public boolean requestHandled = false;
    public int procedure;

    public ProcedureCall() {
        this.responseCode = 500;
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event TODO doc
     * @return TODO doc
     */
    public static ProcedureCall toForward(Event event) {
        return toForward(event, event.getProcedure(), 0);
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event TODO doc
     * @param procedure TODO doc
     */
    public ProcedureCall(Event event, int procedure) {
        this.event = event;
        this.procedure = procedure;
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event TODO doc
     * @param procedure TODO doc
     * @return TODO doc
     */
    public static ProcedureCall toForward(Event event, int procedure) {
        return toForward(event, procedure, 0);
    }

    /**
     * Creates ProcedureCall object to be forwarded to the service for
     * processing.
     *
     * @param event TODO doc
     * @param procedure TODO doc
     * @param responseCode TODO doc
     * @return TODO doc
     */
    public static ProcedureCall toForward(Event event, int procedure, int responseCode) {
        ProcedureCall pc = new ProcedureCall();
        pc.requestHandled = false;
        pc.event = event;
        pc.procedure = procedure;
        pc.responseCode = responseCode;
        return pc;
    }

    /**
     * Creates ProcedureCall object to be processed by the inbound adapter without calling the service.
     * 
     * @param responseCode TODO doc
     * @param responseObject TODO doc
     * @return TODO doc
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
     * @param responseCode TODO doc
     * @param contentType TODO doc
     * @param responseObject TODO doc
     * @return TODO doc
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
