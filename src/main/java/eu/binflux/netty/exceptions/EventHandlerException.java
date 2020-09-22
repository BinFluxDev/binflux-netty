package eu.binflux.netty.exceptions;

public class EventHandlerException extends Throwable {

    public EventHandlerException() {
    }

    public EventHandlerException(String message) {
        super(message);
    }

    public EventHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventHandlerException(Throwable cause) {
        super(cause);
    }
}
