package eu.binflux.netty.serialization;

public interface ISerializer {

    <T> byte[] serialize(T object);

    <T> T deserialize(byte[] bytes);
}
