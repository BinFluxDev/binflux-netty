package eu.binflux.netty.eventhandler.consumer.timeout;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class ReadTimeoutEvent implements ConsumerEvent {

    private ChannelHandlerContext ctx;

    public ReadTimeoutEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
