package eu.binflux.netty.eventhandler.consumer;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class ConnectEvent implements ConsumerEvent {

    ChannelHandlerContext ctx;

    public ConnectEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

}
