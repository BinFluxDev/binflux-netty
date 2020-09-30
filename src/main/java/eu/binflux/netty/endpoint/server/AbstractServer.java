package eu.binflux.netty.endpoint.server;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.endpoint.EndpointType;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractServer extends AbstractEndpoint {

    private int port;

    /**
     * Default constructor of `AbstractEndpoint`
     */
    public AbstractServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder);
        this.port = port;
    }

    /**
     * @param object Send object to client
     */
    public abstract void send(ChannelHandlerContext channelHandlerContext, Object object, boolean sync);

    /**
     * @return returns the `EndpointType`
     */
    @Override
    public EndpointType endpointType() {
        return EndpointType.SERVER;
    }

    /**
     * Sets the port for the server
     */
    public void setPort(int port) {
        if (port > 0)
            this.port = port;
    }
    /**
     * Getter of the port
     */
    public int getPort() {
        return port;
    }
}
