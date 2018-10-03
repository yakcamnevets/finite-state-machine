package ca.yakcam.fsm;

public class StateException extends Exception {
    public StateException(String message) {
        super(message);
    }

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }
}
