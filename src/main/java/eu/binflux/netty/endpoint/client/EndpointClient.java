
package eu.binflux.netty.endpoint.client;

import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.eventhandler.consumer.ErrorEvent;
import eu.binflux.netty.handler.NettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EndpointClient extends AbstractClient {

    public static EndpointClient newInstance(EndpointBuilder builder, String host, int port) {
        return new EndpointClient(builder, host, port);
    }

    private final ExecutorService executor;

    private final EventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;
    private String host;
    private int port;

    protected EndpointClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder);

        this.host = host;
        this.port = port;

        this.executor = Executors.newFixedThreadPool(2 * endpointBuilder.getClientWorkerSize());

        // Note: We don't support KQueue.

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int workerSize = endpointBuilder.getClientWorkerSize() * cores;

        // Check and initialize the event-loop-groups
        this.group = Epoll.isAvailable() ? new EpollEventLoopGroup(workerSize) : new NioEventLoopGroup(workerSize);

        // Create Bootstrap
        this.bootstrap = new Bootstrap()
                .group(this.group)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new NettyInitializer(this))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN_CONNECT, true);
        }
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    @Override
    public void send(Object object, boolean sync) {
        try {
            if (isConnected()) {
                if (sync) {
                    channel.writeAndFlush(object).sync();
                } else {
                    executor.execute(() -> channel.writeAndFlush(object));
                }
            }
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    /**
     * Return if the client is connected or not
     */
    @Override
    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen() && this.channel.isActive();
    }

    /**
     * Close only the channel
     */
    @Override
    public boolean close() {
        if (isConnected()) {
            try {
                channel.close().sync();
                return true;
            } catch (InterruptedException e) {
                eventHandler().handleEvent(new ErrorEvent(e));
            }
        }
        return false;
    }

    /**
     * Close the endpoint
     */
    @Override
    public boolean stop() {
        try {
            eventHandler().unregisterAll();
            group.shutdownGracefully();
            close();
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Connects the client to the given host and port
     */
    @Override
    public boolean start() {
        // Check if host and port is set
        if (host == null || port == -1) {
            eventHandler().handleEvent(new ErrorEvent(new RuntimeException(host == null ? "host-address is not set!" : "port is not set!")));
            return false;
        }

        // Close the Channel if it's already connected
        if (isConnected()) {
            eventHandler().handleEvent(new ErrorEvent(new IllegalStateException("Connection is already established!")));
            return false;
        }

        // Start the client and wait for the connection to be established.
        try {
            this.channel = this.bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
            return true;
        } catch (InterruptedException e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Sets the host and port of the client
     */
    public void setAddress(String host, int port) {
        if (host != null)
            this.host = host;
        if (port > 0)
            this.port = port;
    }

}
