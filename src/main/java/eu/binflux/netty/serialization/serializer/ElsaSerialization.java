package eu.binflux.netty.serialization.serializer;

import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serialization;
import org.mapdb.elsa.ElsaMaker;
import org.mapdb.elsa.ElsaSerializerPojo;

import java.io.*;

public class ElsaSerialization implements Serialization {

    private final ElsaSerializerPojo elsaSerializer;

    public ElsaSerialization() {
        this.elsaSerializer = new ElsaMaker().make();
    }

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(outputStream);
            elsaSerializer.serialize(output, object);
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
            @SuppressWarnings("unchecked")
            T object = (T) elsaSerializer.deserialize(input);
            input.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
