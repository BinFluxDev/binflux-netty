package eu.binflux.netty.eventhandler.consumer.message;


import eu.binflux.netty.eventhandler.ConsumerEvent;

public class LogEvent implements ConsumerEvent {

    String message;

    public LogEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
