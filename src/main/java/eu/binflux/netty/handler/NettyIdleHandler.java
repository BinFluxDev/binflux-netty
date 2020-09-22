package eu.binflux.netty.handler;

import eu.binflux.netty.eventhandler.consumer.ReadTimeoutEvent;
import eu.binflux.netty.eventhandler.consumer.WriteTimeoutEvent;
import eu.binflux.netty.endpoint.AbstractEndpoint;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyIdleHandler extends ChannelDuplexHandler {

    private final AbstractEndpoint endpoint;

    public NettyIdleHandler(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE)
                if (endpoint.eventHandler().hasConsumer(WriteTimeoutEvent.class))
                    endpoint.eventHandler().handleEvent(new WriteTimeoutEvent(ctx));
                else // Default behaviour: `send every 15 sec int 1`
                    ctx.writeAndFlush(1);
            else if (idleStateEvent.state() == IdleState.READER_IDLE)
                if (endpoint.eventHandler().hasConsumer(ReadTimeoutEvent.class))
                    endpoint.eventHandler().handleEvent(new ReadTimeoutEvent(ctx));
        }
        super.userEventTriggered(ctx, evt);
    }
}
