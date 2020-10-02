package eu.binflux.netty.serialization;

public interface Serialization {

    <T> byte[] serialize(T object);

    <T> T deserialize(byte[] bytes);

}
