package eu.binflux.netty.endpoint.client;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.endpoint.EndpointBuilder;
import eu.binflux.netty.endpoint.EndpointType;

public abstract class AbstractClient extends AbstractEndpoint {

    /**
     * Default constructor of `AbstractEndpoint`
     */
    public AbstractClient(EndpointBuilder endpointBuilder) {
        super(endpointBuilder);
    }

    /**
     * @param object Send object to server
     * @param sync Handle `send`-call sync or async
     */
    public abstract void send(Object object, boolean sync);

    /**
     * @param object Send object to server
     */
    public void send(Object object) {
        send(object, false);
    }

    /**
     * @return Check if client is connected to server
     */
    public abstract boolean isConnected();

    /**
     * @return returns the `EndpointType`
     */
    @Override
    public EndpointType endpointType() {
        return EndpointType.CLIENT;
    }
}
