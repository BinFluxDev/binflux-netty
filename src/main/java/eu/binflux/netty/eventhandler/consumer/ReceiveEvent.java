package eu.binflux.netty.eventhandler.consumer;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class ReceiveEvent implements ConsumerEvent {

    ChannelHandlerContext ctx;
    Object object;

    public ReceiveEvent(ChannelHandlerContext ctx, Object object) {
        this.ctx = ctx;
        this.object = object;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public Object getObject() {
        return object;
    }
}
