package eu.binflux.netty.handler;

import eu.binflux.netty.endpoint.AbstractEndpoint;
import eu.binflux.netty.eventhandler.consumer.ErrorEvent;
import eu.binflux.netty.exceptions.SerializationException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class NettyCodec extends ByteToMessageCodec<Object> {

    private final AbstractEndpoint endpoint;

    public NettyCodec(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) {
        try {
            // Encode object to byte[]
            byte[] outArray = endpoint.builder().serialize(object);

            // Write byte[] length to ByteBuf
            writeVarInt(out, outArray.length);

            // Write data-content to ByteBuf
            out.writeBytes(outArray);
        } catch (Exception e) {
            endpoint.eventHandler().handleEvent(new ErrorEvent(new SerializationException("Error while encoding:", e)));
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // Check if contains VarInt-Length
            if (in.readableBytes() < 5) {
                return;
            }

            // Mark the reader-index
            in.markReaderIndex();

            // Decode VarIntLength
            int contentLength = readVarInt(in);

            // Is bytes exceeds length -> resetReaderIndex
            if (in.readableBytes() < contentLength) {
                in.resetReaderIndex();
                return;
            }

            // Read content as byte[]
            byte[] packetContent = new byte[contentLength];
            in.readBytes(packetContent);

            // Read content as byte[]
            Object object = endpoint.builder().deserialize(packetContent);

            // Add object to output-list
            out.add(object);
        } catch (Exception e) {
            endpoint.eventHandler().handleEvent(new ErrorEvent(new SerializationException("Error while decoding:", e)));
        }
    }

    /**
     * Method to read variable length from ByteBuf
     *
     * @param buf The ByteBuf to read
     * @return the variable int
     */
    private int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;
        while (true) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    /**
     * Method to write variable length to ByteBuf
     *
     * @param buf The ByteBuf to write
     * @param value The int to write
     */
    private void writeVarInt(ByteBuf buf, int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                buf.writeByte(value);
                return;
            }
            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }
}
