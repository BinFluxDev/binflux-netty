package eu.binflux.netty.eventhandler.consumer;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectEvent implements ConsumerEvent {

    ChannelHandlerContext ctx;

    public DisconnectEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
