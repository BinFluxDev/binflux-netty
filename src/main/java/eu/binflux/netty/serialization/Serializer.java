package eu.binflux.netty.serialization;

public interface Serializer {

    <T> byte[] serialize(T object);

    <T> T deserialize(byte[] bytes);

}
