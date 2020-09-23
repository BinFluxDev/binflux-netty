
package eu.binflux.netty.endpoint;

import eu.binflux.netty.eventhandler.EventHandler;

public abstract class AbstractEndpoint implements Endpoint {

	private final EndpointBuilder endpointBuilder;
	private final EventHandler eventHandler;

	public AbstractEndpoint(EndpointBuilder endpointBuilder) {
		this.endpointBuilder = endpointBuilder;
		this.eventHandler = new EventHandler();
	}

	/**
	 * @return returns the `EndpointBuilder`
	 */
	@Override
	public EndpointBuilder builder() {
		return endpointBuilder;
	}

	/**
	 * @return returns the `EventHandler`
	 */
	@Override
	public EventHandler eventHandler() {
		return this.eventHandler;
	}


}
