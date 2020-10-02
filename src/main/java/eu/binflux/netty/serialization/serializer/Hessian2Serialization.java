package eu.binflux.netty.serialization.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import eu.binflux.netty.exceptions.SerializerException;
import eu.binflux.netty.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Hessian2Serialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            if(!(object instanceof Serializable))
                throw new SerializerException("Object doesn't implement Serializable");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(outputStream);
            output.startMessage();
            output.writeObject(object);
            output.completeMessage();
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
            Hessian2Input input = new Hessian2Input(inputStream);
            input.startMessage();
            @SuppressWarnings("unchecked")
            T object = (T) input.readObject();
            input.completeMessage();
            input.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
