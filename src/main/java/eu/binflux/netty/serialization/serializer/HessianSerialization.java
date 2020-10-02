package eu.binflux.netty.serialization.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class HessianSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HessianOutput output = new HessianOutput(outputStream);
            output.writeObject(object);
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
            HessianInput input = new HessianInput(inputStream);
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
