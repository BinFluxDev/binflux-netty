package eu.binflux.netty.endpoint.client;

import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.eventhandler.consumer.ErrorEvent;
import eu.binflux.netty.handler.NettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.*;

public class PooledClient extends AbstractClient {

    private final ExecutorService executor;

    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private int poolSize;

    private final Set<Channel> activeConnectionSet;
    private final BlockingQueue<Channel> freeConnectionDeque;

    public PooledClient(EndpointBuilder endpointBuilder, String host, int port, int poolSize) {
        super(endpointBuilder, host, port);

        // Set final fields
        this.poolSize = poolSize;
        this.executor = Executors.newFixedThreadPool(2 * this.poolSize);

        // Initialize the ActiveChannelList. We need a thread-safe-set
        this.activeConnectionSet = ConcurrentHashMap.newKeySet();
        // Initialize the FreeChannelQueue. We need a thread-safe-blocking-queue
        this.freeConnectionDeque = new LinkedBlockingQueue<>();

        // Note: We don't support KQueue. Boycott OSX and FreeBSD :P

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int workerSize = endpointBuilder.getClientWorkerSize() * cores;

        // Check and initialize the event-loop-groups
        this.eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(workerSize) : new NioEventLoopGroup(workerSize);

        // Create Bootstrap
        this.bootstrap = new Bootstrap()
                .group(this.eventLoopGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new NettyInitializer(this))
                .remoteAddress(new InetSocketAddress(getHost(), getPort()))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (Epoll.isAvailable())
            this.bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN_CONNECT, true);

    }


    /**
     * Starts the preconnect for the channel-pool.
     */
    @Override
    public boolean start() {
        try {
            // Start for-loop with poolSize from constructor
            for (int i = 1; i < poolSize; i++) {
                newChannel();
            }
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Stops the client.
     */
    @Override
    public boolean stop() {
        try {
            eventHandler().unregisterAll();
            activeConnectionSet.forEach(Channel::close);
            freeConnectionDeque.forEach(Channel::close);
            eventLoopGroup.shutdownGracefully();
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Closes the client-channels.
     */
    @Override
    public boolean close() {
        try {
            activeConnectionSet.forEach(Channel::close);
            freeConnectionDeque.forEach(Channel::close);
            return true;
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return false;
    }

    /**
     * Creates a new channel into the pool
     */
    private Channel newChannel() {
        try {
            // Connect new channel
            Channel channel = this.bootstrap.connect(new InetSocketAddress(getHost(), getPort())).await().channel();

            // Add it to the connection-list
            activeConnectionSet.add(channel);

            // Add a listener to the closeFuture of the channel,
            // to remove it from connection-list, if it closes
            channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> activeConnectionSet.remove(closeFuture.channel()));
            return channel;
        } catch (InterruptedException e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
        return null;
    }

    /**
     * Obtains a channel object from the pool
     * or creates a new one if the pool isn't full
     */
    private Channel obtain() {
        synchronized (activeConnectionSet) {
            if (freeConnectionDeque.isEmpty() && activeConnectionSet.size() < poolSize) {
                return newChannel();
            }
            try {
                return freeConnectionDeque.take();
            } catch (Exception e) {
                eventHandler().handleEvent(new ErrorEvent(e));
            }
        }
        return null;
    }


    /**
     * Releases the channel back to the pool
     *
     * @param channel The Channel.class object from #obtain()
     */
    private void free(Channel channel) {
        try {
            this.freeConnectionDeque.put(channel);
        } catch (InterruptedException e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    @Override
    public boolean isConnected() {
        return !this.activeConnectionSet.isEmpty();
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    @Override
    public void send(Object object, boolean sync) {
        try {
            if (isConnected()) {
                if (sync) {
                    fireAndSync(object);
                } else {
                    executor.execute(() -> fireAndForget(object));
                }
            }
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    private void fireAndForget(Object object) {
        try {
            Channel channel = obtain();
            channel.writeAndFlush(object);
            free(channel);
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    private void fireAndSync(Object object) {
        try {
            Channel channel = obtain();
            channel.writeAndFlush(object).sync();
            free(channel);
        } catch (Exception e) {
            eventHandler().handleEvent(new ErrorEvent(e));
        }
    }

    /**
     * Sets the poolSize of the client
     */
    public void setPoolSize(int poolSize) {
        if (poolSize > 0)
            this.poolSize = poolSize;
    }

    /**
     * Getter for int poolSize
     */
    public int getPoolSize() {
        return poolSize;
    }
}
