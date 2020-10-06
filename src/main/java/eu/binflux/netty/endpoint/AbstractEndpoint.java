
package eu.binflux.netty.endpoint;

import eu.binflux.netty.eventhandler.EventHandler;
import eu.binflux.netty.eventhandler.consumer.ReceiveEvent;
import eu.binflux.netty.eventhandler.consumer.message.ErrorEvent;
import eu.binflux.netty.protocol.ErrorEventConsumer;
import eu.binflux.netty.protocol.packet.PacketReceiveConsumer;

public abstract class AbstractEndpoint implements Endpoint {

	private final EndpointBuilder endpointBuilder;
	private final EventHandler eventHandler;

	public AbstractEndpoint(EndpointBuilder endpointBuilder) {
		this.endpointBuilder = endpointBuilder;
		this.eventHandler = new EventHandler();
		if (endpointBuilder.isPacketProtocol())
			eventHandler().registerConsumer(ReceiveEvent.class, new PacketReceiveConsumer(this));
			eventHandler().registerConsumer(ErrorEvent.class, new ErrorEventConsumer());
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
