package eu.binflux.netty.eventhandler.consumer.message;


import eu.binflux.netty.eventhandler.ConsumerEvent;

public class DebugEvent implements ConsumerEvent {

    String message;

    public DebugEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
