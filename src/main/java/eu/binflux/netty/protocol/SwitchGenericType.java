package eu.binflux.netty.protocol;

import java.util.Optional;
import java.util.function.Consumer;

public class SwitchGenericType {

    static public <T> void cswitch(Object object, Consumer... consumers) {
        for (Consumer consumer : consumers) {
            consumer.accept(object);
        }
    }

    static public <T> Consumer ccase(Class<T> clazz, Consumer<T> consumer) {
        return object -> Optional.of(object).filter(clazz::isInstance).map(clazz::cast).ifPresent(consumer);
    }
}
