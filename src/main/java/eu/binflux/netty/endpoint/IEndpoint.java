package eu.binflux.netty.endpoint;

import eu.binflux.netty.eventhandler.EventHandler;

public interface IEndpoint {

    /**
     * @return Returns the Endpoint-Options instance
     */
    EndpointBuilder builder();

    /**
     * @return Returns the Endpoint-NetworkEventManager instance
     */
    EventHandler eventHandler();

    /**
     * @return Starts the Endpoint-connection and returns true if was successful
     */
    boolean start();

    /**
     * @return Stops the Endpoint-connection and returns true if was successful
     */
    boolean stop();

    /**
     * @return closes the Endpoint-connection and returns true if was successful
     */
    boolean close();

    /**
     * @return The Endpoint-type. CLIENT or SERVER
     */
    EndpointType endpointType();
}
