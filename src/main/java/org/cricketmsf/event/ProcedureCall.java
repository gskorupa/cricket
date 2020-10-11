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
    
    public ProcedureCall(){
        this.responseCode = 500;
    }

    public ProcedureCall(Event event, String procedureName) {
        this.event = event;
        this.procedureName = procedureName;
    }
    
    public static ProcedureCall forward(Event event, String procedureName) {
        return forward(event, procedureName, 0);
    }
    
    public static ProcedureCall forward(Event event, String procedureName, int responseCode) {
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = false;
        pc.event = event;
        pc.procedureName = procedureName;
        pc.responseCode = responseCode;
        return pc;
    }
    
    public static ProcedureCall respond(int responseCode, Object responseObject){
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = true;
        pc.response=responseObject;
        pc.responseCode=responseCode;
        return pc;
    }
        
    public static ProcedureCall respond(int responseCode, String contentType, Object responseObject){
        ProcedureCall pc=new ProcedureCall();
        pc.requestHandled = true;
        pc.contentType=contentType;
        pc.response=responseObject;
        pc.responseCode=responseCode;
        return pc;
    }

}
