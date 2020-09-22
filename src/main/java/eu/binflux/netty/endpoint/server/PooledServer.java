
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PooledServer extends AbstractServer {

    public static PooledServer newInstance(EndpointBuilder builder, int port, int poolSize) {
        return new PooledServer(builder, port, poolSize);
    }

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Set<Channel> serverSocketList;
    private int poolSize;
    private int port;

    private PooledServer(EndpointBuilder endpointBuilder, int port, int poolSize) {
        super(endpointBuilder);

        this.poolSize = poolSize;
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
        this.bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new NettyInitializer(this))
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            bootstrap
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN, 3)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }

        // Initialize the ServerSocketList. We need a thread-safe-set
        this.serverSocketList = ConcurrentHashMap.newKeySet();
    }

    /**
     * Starts the server socket.
     */
    @Override
    public boolean start() {
        try {
            for (int i = 1; i <= poolSize; i++) {
                Channel channel = bootstrap.bind(new InetSocketAddress(port)).sync().channel();
                serverSocketList.add(channel);
                channel.closeFuture().addListener((future) -> serverSocketList.remove(channel));
            }
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Stops the server
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
            serverSocketList.forEach(Channel::close);

            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Closes the server socket.
     */
    @Override
    public boolean close() {
        try {
            // close server-channel
            serverSocketList.forEach(Channel::close);

            return true;
        } catch (Exception e) {
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
        if (sync) {
            try {
                ctx.writeAndFlush(object).sync();
            } catch (InterruptedException e) {
                eventHandler().handleEvent(new ErrorEvent(e));
            }
        } else {
            ctx.writeAndFlush(object);
        }
    }

    /**
     * Sets the port and poolSize of the server
     */
    public void setPort(int port, int poolSize) {
        if (port > 0)
            this.port = port;
        if(poolSize > 0)
            this.poolSize = poolSize;
    }

}
