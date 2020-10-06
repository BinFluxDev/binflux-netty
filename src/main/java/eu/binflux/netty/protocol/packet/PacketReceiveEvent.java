package eu.binflux.netty.protocol.packet;

import eu.binflux.netty.eventhandler.ConsumerEvent;
import io.netty.channel.ChannelHandlerContext;

public class PacketReceiveEvent implements ConsumerEvent {

    ChannelHandlerContext ctx;
    Packet packet;

    public PacketReceiveEvent(ChannelHandlerContext ctx, Packet packet) {
        this.ctx = ctx;
        this.packet = packet;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public Packet getPacket() {
        return packet;
    }
}
