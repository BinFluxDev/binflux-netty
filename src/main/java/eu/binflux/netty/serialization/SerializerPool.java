package eu.binflux.netty.serialization;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.lang.reflect.ParameterizedType;

public class SerializerPool<T extends AbstractSerializer> implements SerializedPool<AbstractSerializer> {

    private GenericObjectPool<T> objectPool;

    public SerializerPool() {
        Class<T> classOfT = (Class<T>)
                ((ParameterizedType)getClass()
                        .getGenericSuperclass())
                        .getActualTypeArguments()[0];
        this.objectPool = new GenericObjectPool<T>(new Factory<>(classOfT));
    }

    @Override
    public void free(AbstractSerializer serializer) {
        objectPool.returnObject((T) serializer);
    }

    @Override
    public AbstractSerializer obtain() {
        try {
            return objectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Factory<T extends AbstractSerializer> extends BasePooledObjectFactory<T> {

        final Class<T> classOfT;
        final ConstructorAccess<T> constructorAccess;

        public Factory(Class<T> classOfT) {
            this.classOfT = classOfT;
            this.constructorAccess = ConstructorAccess.get(classOfT);
        }

        @Override
        public T create() throws Exception {
            return constructorAccess.newInstance();
        }

        @Override
        public PooledObject<T> wrap(T object) {
            return new DefaultPooledObject<>(object);
        }
    }
}
