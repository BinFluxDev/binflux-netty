package eu.binflux.netty.serialization.serializer;

import eu.binflux.netty.exceptions.SerializationException;
import eu.binflux.netty.serialization.Serialization;

import java.io.*;

public class QuickserSerialization implements Serialization {

    private final com.romix.quickser.Serialization serialization;

    public QuickserSerialization() {
        this.serialization = new com.romix.quickser.Serialization();
    }

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializationException("Object doesn't implement Serializable");
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
