package eu.binflux.netty.eventhandler.consumer.message;

import eu.binflux.netty.eventhandler.ConsumerEvent;

public class ErrorEvent implements ConsumerEvent {

    Throwable throwable;

    public ErrorEvent(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}

