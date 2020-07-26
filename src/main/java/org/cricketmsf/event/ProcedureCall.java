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
    public String contentType = "application/json";
    public boolean requestHandled = false;
    
    public ProcedureCall(){
        this.responseCode = 500;
    }

    public ProcedureCall(EventDecorator event, String procedureName) {
        this.event = event;
        this.procedureName = procedureName;
    }

    /*
    public ProcedureCall(int responseCode, Object errorResponse) {
        this.responseCode = responseCode;
        this.errorResponse = errorResponse;
    }
    
    public ProcedureCall(boolean handled, int responseCode, String contentType, Object errorResponse) {
        this.requestHandled = handled;
        this.contentType = contentType;
        this.responseCode = responseCode;
        this.errorResponse = errorResponse;
    }
    */
    
    public static ProcedureCall forward(EventDecorator event, String procedureName) {
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = false;
        pc.event = event;
        pc.procedureName = procedureName;
        return pc;
    }

    public static ProcedureCall response(int responseCode, Object responseObject){
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = true;
        pc.errorResponse=responseObject;
        return pc;
    }
    
    public static ProcedureCall response(int responseCode, String contentType, Object responseObject){
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = true;
        pc.contentType=contentType;
        pc.errorResponse=responseObject;
        return pc;
    }

}
