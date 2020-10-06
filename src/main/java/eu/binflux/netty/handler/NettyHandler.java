package eu.binflux.netty.handler;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.eventhandler.consumer.ConnectEvent;
import eu.binflux.netty.eventhandler.consumer.DisconnectEvent;
import eu.binflux.netty.eventhandler.consumer.ReceiveEvent;
import eu.binflux.netty.eventhandler.consumer.message.ErrorEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class NettyHandler extends ChannelInboundHandlerAdapter {

    final AbstractEndpoint endpoint;

    public NettyHandler(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            super.channelActive(ctx);
            endpoint.eventHandler().handleEvent(new ConnectEvent(ctx));
        } catch (Exception e) {
            endpoint.eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            super.channelInactive(ctx);
            endpoint.eventHandler().handleEvent(new DisconnectEvent(ctx));
        } catch (Exception e) {
            endpoint.eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            super.channelRead(ctx, msg);
            ctx.executor().execute(() -> endpoint.eventHandler().handleEvent(new ReceiveEvent(ctx, msg)));
        } catch (Exception e) {
            ReferenceCountUtil.release(msg);
            endpoint.eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        try {
            super.channelReadComplete(ctx);
            ctx.flush();
        } catch (Exception e) {
            endpoint.eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        endpoint.eventHandler().handleEvent(new ErrorEvent(cause));
    }
}