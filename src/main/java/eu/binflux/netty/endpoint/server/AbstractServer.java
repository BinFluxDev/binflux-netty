package eu.binflux.netty.endpoint.server;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.endpoint.EndpointType;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractServer extends AbstractEndpoint {

    /**
     * Default constructor of `AbstractEndpoint`
     */
    public AbstractServer(EndpointBuilder endpointBuilder) {
        super(endpointBuilder);
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

}
