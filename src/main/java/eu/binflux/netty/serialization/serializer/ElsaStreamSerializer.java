package eu.binflux.netty.serialization.serializer;

import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serializer;
import org.mapdb.elsa.ElsaObjectInputStream;
import org.mapdb.elsa.ElsaObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class ElsaStreamSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ElsaObjectOutputStream output = new ElsaObjectOutputStream(outputStream);
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
            ElsaObjectInputStream input = new ElsaObjectInputStream(inputStream);
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
