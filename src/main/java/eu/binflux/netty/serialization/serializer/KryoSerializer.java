package eu.binflux.netty.serialization.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import eu.binflux.netty.serialization.ISerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements ISerializer {

    private static final int DEFAULT_INPUT_BUFFER_SIZE = 262144;
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 262144;
    private static final int DEFAULT_MAX_OUTPUT_BUFFER_SIZE = 524288;

    private final Pool<Kryo> kryoPool;
    private final Pool<Input> inputPool;
    private final Pool<Output> outputPool;

    public KryoSerializer() {
        this(DEFAULT_INPUT_BUFFER_SIZE, DEFAULT_OUTPUT_BUFFER_SIZE, DEFAULT_MAX_OUTPUT_BUFFER_SIZE);
    }

    public KryoSerializer(int inputBufferSize, int outputBufferSize, int maxOutputSize) {

        // Initialize Kryo-Pool
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                // Create new Kryo instance
                Kryo kryo = new Kryo();
                kryo.setRegistrationRequired(false);
                kryo.setReferences(true);
                kryo.setOptimizedGenerics(true);
                kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                return kryo;
            }
        };

        // Initialize Input-Pool
        inputPool = new Pool<Input>(true, true) {
            @Override
            protected Input create() {
                return new Input(inputBufferSize);
            }
        };

        // Initialize Output-Pool
        outputPool = new Pool<Output>(true, true) {
            @Override
            protected Output create() {
                return new Output(outputBufferSize, maxOutputSize);
            }
        };
    }

    @Override
    public <T> byte[] serialize(T object) {
        Kryo kryo = kryoPool.obtain();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = outputPool.obtain();
        output.setOutputStream(outputStream);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        kryoPool.free(kryo);
        outputPool.free(output);
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        Kryo kryo = kryoPool.obtain();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Input input = inputPool.obtain();
        input.setInputStream(inputStream);
        @SuppressWarnings("unchecked")
        T object = (T) kryo.readClassAndObject(input);
        input.close();
        kryoPool.free(kryo);
        inputPool.free(input);
        return object;
    }
}
