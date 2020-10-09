package eu.binflux.netty.endpoint;

import eu.binflux.netty.endpoint.client.EndpointClient;
import eu.binflux.netty.endpoint.client.PooledClient;
import eu.binflux.netty.endpoint.server.EndpointServer;
import eu.binflux.netty.endpoint.server.PooledServer;
import eu.binflux.serializer.SerializerPool;
import eu.binflux.serializer.serialization.KryoSerialization;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class EndpointBuilder {

    private final AtomicBoolean logging;

    private final AtomicBoolean packetProtocol;
    private final AtomicBoolean printErrors;

    private final AtomicBoolean eventExecutor;
    private final AtomicInteger eventExecutorSize;

    private final AtomicBoolean idleState;
    private final AtomicInteger readTimeout;
    private final AtomicInteger writeTimeout;

    private final AtomicInteger clientWorkerSize;

    private final AtomicInteger serverBossSize;
    private final AtomicInteger serverWorkerSize;

    private SerializerPool pooledSerializer;

    private EndpointBuilder() {
        this.logging = new AtomicBoolean(false);

        this.packetProtocol = new AtomicBoolean(false);
        this.printErrors = new AtomicBoolean(false);
        this.eventExecutor = new AtomicBoolean(false);
        this.eventExecutorSize = new AtomicInteger(0);

        this.idleState = new AtomicBoolean(false);
        this.readTimeout = new AtomicInteger(0);
        this.writeTimeout = new AtomicInteger(0);

        this.clientWorkerSize = new AtomicInteger(2);

        this.serverBossSize = new AtomicInteger(1);
        this.serverWorkerSize = new AtomicInteger(5);

        this.pooledSerializer = new SerializerPool(KryoSerialization.class);

    }

    public static EndpointBuilder newBuilder() {
        return new EndpointBuilder();
    }

    public EndpointBuilder logging(boolean value) {
        this.logging.set(value);
        return this;
    }

    public EndpointBuilder packetProtocol(boolean value) {
        this.packetProtocol.set(value);
        return this;
    }

    public EndpointBuilder printErrors(boolean value) {
        this.printErrors.set(true);
        return this;
    }

    public EndpointBuilder eventExecutor(int size) {
        this.eventExecutor.set(true);
        this.eventExecutorSize.set(size);
        return this;
    }

    public EndpointBuilder idleState(int readTimeout, int writeTimeout) {
        this.idleState.set(true);
        this.readTimeout.set(readTimeout);
        this.writeTimeout.set(writeTimeout);
        return this;
    }

    public EndpointBuilder clientWorkerGroup(int size) {
        this.clientWorkerSize.set(size);
        return this;
    }

    public EndpointBuilder serverWorkerGroup(int size) {
        this.serverWorkerSize.set(size);
        return this;
    }

    public EndpointBuilder serverBossGroup(int size) {
        this.serverBossSize.set(size);
        return this;
    }

    public SerializerPool getPooledSerializer() {
        return pooledSerializer;
    }

    public EndpointBuilder serializer(SerializerPool pooledSerializer) {
        this.pooledSerializer = pooledSerializer;
        return this;
    }

    public <T> byte[] serialize(T object) {
        return pooledSerializer.serialize(object);
    }

    public <T> T deserialize(byte[] bytes) {
        return pooledSerializer.deserialize(bytes);
    }

    public boolean isLogging() {
        return this.logging.get();
    }

    public boolean isPacketProtocol() {
        return this.packetProtocol.get();
    }

    public boolean isPrintErrors() {
        return this.printErrors.get();
    }

    public boolean isEventExecutor() {
        return this.eventExecutor.get();
    }

    public int getEventExecutorSize() {
        return this.eventExecutorSize.get();
    }

    public boolean isIdleState() {
        return this.idleState.get();
    }

    public int getReadTimeout() {
        return this.readTimeout.get();
    }

    public int getWriteTimeout() {
        return this.writeTimeout.get();
    }

    public int getClientWorkerSize() {
        return this.clientWorkerSize.get();
    }

    public int getServerWorkerSize() {
        return this.serverWorkerSize.get();
    }

    public int getServerBossSize() {
        return this.serverBossSize.get();
    }

    public EndpointClient build(String host, int port) {
        return new EndpointClient(this, host, port);
    }

    public EndpointServer build(int port) {
        return new EndpointServer(this, port);
    }

    public PooledClient build(String host, int port, int poolSize) {
        return new PooledClient(this, host, port, poolSize);
    }

    public PooledServer build(int port, int poolSize) {
        return new PooledServer(this, port, poolSize);
    }
}
