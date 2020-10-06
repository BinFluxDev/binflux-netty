package eu.binflux.netty.protocol.packet;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.eventhandler.consumer.ReceiveEvent;

import java.util.function.Consumer;

public class PacketReceiveConsumer implements Consumer<ReceiveEvent> {

    final AbstractEndpoint endpoint;

    public PacketReceiveConsumer(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void accept(ReceiveEvent receiveEvent) {
        if(receiveEvent.getObject() instanceof Packet) {
            Packet packet = (Packet) receiveEvent.getObject();
            endpoint.eventHandler().handleEvent(new PacketReceiveEvent(receiveEvent.getCtx(), packet));
        }
    }
}
