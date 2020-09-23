package eu.binflux.netty.endpoint.server;

import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.eventhandler.consumer.ErrorEvent;
import eu.binflux.netty.handler.NettyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EndpointServer extends AbstractServer {

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel channel;
    private int port;

    public EndpointServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder);

        this.port = port;

        // Note: We don't support KQueue.

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int bossSize = endpointBuilder.getServerBossSize() * cores;
        int workerSize = endpointBuilder.getServerWorkerSize() * cores;

        // Check and initialize the event-loop-groups
        this.bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(bossSize) : new NioEventLoopGroup(bossSize);
        this.workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(workerSize) : new NioEventLoopGroup(workerSize);

        // Create ServerBootstrap
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new NettyInitializer(this))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true);

        // Set KeepAlive option if it's enabled
        if (endpointBuilder.isIdleState())
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            serverBootstrap
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN, 3)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }
    }

    /**
     * Stops the server socket.
     */
    @Override
    public boolean stop() {
        try {
            // unregister network-events
            eventHandler().unregisterAll();

            // shutdown eventloop-groups
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            // close server-channel
            channel.close().sync();
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Let the server bind to the given port
     */
    @Override
    public boolean start() {
        if (port == -1) {
            eventHandler().handleEvent(new ErrorEvent(new RuntimeException("port is not set")));
            return false;
        }
        try {
            // Start the server and wait for socket to be bind to the given port
            this.channel = serverBootstrap.bind(new InetSocketAddress(port)).sync().channel();
            return true;
        } catch (InterruptedException e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Closes the server socket.
     */
    @Override
    public boolean close() {
        if (channel != null && channel.isOpen())
            try {
                channel.close().sync();
                return true;
            } catch (InterruptedException e) {
                eventHandler().handleEvent(new ErrorEvent(e));
            }
        return false;
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public void send(ChannelHandlerContext ctx, Object object) {
        // use send-method, default-behaviour: async
        send(ctx, object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    @Override
    public void send(ChannelHandlerContext ctx, Object object, boolean sync) {
        if (sync)
            try {
                ctx.writeAndFlush(object).sync();
            } catch (InterruptedException e) {
                eventHandler().handleEvent(new ErrorEvent(e));
            }
        else
            ctx.writeAndFlush(object);
    }

    /**
     * Sets the port for the server
     */
    public void setPort(int port) {
        if (port > 0)
            this.port = port;
    }

}
