package eu.binflux.netty.serialization.serializer;

import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.ISerializer;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;

public class FSTSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FSTObjectOutput output = new FSTObjectOutput(outputStream);
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
            FSTObjectInput input = new FSTObjectInput(inputStream);
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
