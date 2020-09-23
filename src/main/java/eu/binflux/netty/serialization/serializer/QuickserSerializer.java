package eu.binflux.netty.serialization.serializer;

import com.romix.quickser.Serialization;
import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serializer;

import java.io.Serializable;

public class QuickserSerializer implements Serializer {

    private final Serialization serialization;

    public QuickserSerializer() {
        this.serialization = new Serialization();
    }

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            return serialization.serialize(object);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        try {
            return (T) serialization.deserialize(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
