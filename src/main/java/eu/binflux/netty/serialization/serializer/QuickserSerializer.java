package eu.binflux.netty.serialization.serializer;

import com.romix.quickser.Serialization;
import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serializer;

import java.io.*;

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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(outputStream);
            serialization.serialize(output, object);
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
            DataInputStream input = new DataInputStream(inputStream);
            T object = (T) serialization.deserialize(input);
            input.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
