package eu.binflux.netty.handler;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.endpoint.EndpointType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class NettyInitializer extends ChannelInitializer<SocketChannel> {

    private final AbstractEndpoint endpoint;
    private final EventExecutorGroup executorGroup;

    public NettyInitializer(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;

        // Initialize EventExecutorGroup if execution is enabled
        if (endpoint.builder().isEventExecutor())
            this.executorGroup = new DefaultEventExecutorGroup(endpoint.builder().getEventExecutorSize());
        else
            this.executorGroup = null;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // Get pipeline-instance
        ChannelPipeline pipeline = ch.pipeline();

        // Add logging-handler if enabled
        if (endpoint.builder().isLogging())
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));

        /*
        Only set the IdleHandler on the clients.
        Set Read-/Write-Timeout to 0 to disable.
        default ReadTime=0 and WriteTime=15
        */
        if (endpoint.endpointType() == EndpointType.CLIENT && endpoint.builder().isIdleState()) {
            pipeline.addLast("idle-state", new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
            pipeline.addLast("idle-handler", new NettyIdleHandler(endpoint));
        }

        // TODO: Add more serializer-options
        // Add the kryo ByteToMessageCodec<Object>
        pipeline.addLast("netty-codec", new NettyCodec(endpoint));

        // Add the business-logic handler. (async or async)
        // And let it execute by DefaultEventExecutorGroup if is set
        if (endpoint.builder().isEventExecutor())
            pipeline.addLast(executorGroup, "netty-handler", new NettyHandler(endpoint));
        else
            pipeline.addLast("netty-handler", new NettyHandler(endpoint));
    }
}