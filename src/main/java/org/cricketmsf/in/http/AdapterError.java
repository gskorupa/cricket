package org.cricketmsf.in.http;

/**
 *
 * @author greg
 */
public class AdapterError {

    public int code;
    public String message;

    public AdapterError() {
    }

    public AdapterError(int errorCode, String message) {
        this.code = errorCode;
        this.message = message;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
