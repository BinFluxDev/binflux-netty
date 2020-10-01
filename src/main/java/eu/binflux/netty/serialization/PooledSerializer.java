package eu.binflux.netty.serialization;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class PooledSerializer extends GenericObjectPool {

    final Class<? extends Serializer> classOfSerial;

    public PooledSerializer(Class<? extends Serializer> classOfSerial) {
        super(new Factory(classOfSerial));
        this.classOfSerial = classOfSerial;
    }

    public Serializer obtain() {
        try {
            return classOfSerial.cast(borrowObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void free(Serializer serializer) {
        returnObject(serializer);
    }

    public static class Factory extends BasePooledObjectFactory {

        final Class<? extends Serializer> classOfSerial;

        public Factory(Class<? extends Serializer> classOfSerial) {
            this.classOfSerial = classOfSerial;
        }


        @Override
        public Object create() throws Exception {
            return classOfSerial.newInstance();
        }

        @Override
        public PooledObject wrap(Object obj) {
            return new DefaultPooledObject(obj);
        }
    }
}
