package eu.binflux.netty.eventhandler.consumer;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class WriteTimeoutEvent implements ConsumerEvent {

    private ChannelHandlerContext ctx;

    public WriteTimeoutEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
