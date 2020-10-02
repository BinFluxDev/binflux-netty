package eu.binflux.netty.serialization.serializer;

import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serialization;
import org.nustaq.serialization.FSTObjectInputNoShared;
import org.nustaq.serialization.FSTObjectOutputNoShared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class FSTNoSharedSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FSTObjectOutputNoShared output = new FSTObjectOutputNoShared(outputStream);
            output.writeObject(object);
            output.flush();
            output.close();
            return outputStream.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            FSTObjectInputNoShared input = new FSTObjectInputNoShared(inputStream);
            @SuppressWarnings("unchecked")
            T object = (T) input.readObject();
            input.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
