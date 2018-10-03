package ca.yakcam.fsm;

public class StateMapException extends Exception {
    public StateMapException(String message) {
        super(message);
    }

    public StateMapException(String message, Throwable cause) {
        super(message, cause);
    }
}
