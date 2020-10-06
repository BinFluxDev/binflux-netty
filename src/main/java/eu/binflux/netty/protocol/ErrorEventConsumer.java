package eu.binflux.netty.protocol;

import eu.binflux.netty.eventhandler.consumer.message.ErrorEvent;

import java.util.function.Consumer;

public class ErrorEventConsumer implements Consumer<ErrorEvent> {

    @Override
    public void accept(ErrorEvent errorEvent) {
        errorEvent.getThrowable().printStackTrace();
    }
}
