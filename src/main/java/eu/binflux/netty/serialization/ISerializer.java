package eu.binflux.netty.serialization;

public interface ISerializer {

    <T> byte[] serialize(Object object);

    <T> T deserialize(byte[] bytes);
}
