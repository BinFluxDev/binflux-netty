package eu.binflux.netty.serialization;

public interface SerializedPool<AbstractSerializer> {

    AbstractSerializer obtain();

    void free(AbstractSerializer serializer);
}
