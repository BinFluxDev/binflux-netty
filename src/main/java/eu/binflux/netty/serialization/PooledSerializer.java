package eu.binflux.netty.serialization;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class PooledSerializer extends GenericObjectPool {

    final Class<? extends Serialization> classOfSerial;

    public PooledSerializer(Class<? extends Serialization> classOfSerial) {
        super(new Factory(classOfSerial));
        this.classOfSerial = classOfSerial;
    }

    public Serialization obtain() {
        try {
            return classOfSerial.cast(borrowObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void free(Serialization serialization) {
        returnObject(serialization);
    }

    public static class Factory extends BasePooledObjectFactory {

        final Class<? extends Serialization> classOfSerial;

        public Factory(Class<? extends Serialization> classOfSerial) {
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
