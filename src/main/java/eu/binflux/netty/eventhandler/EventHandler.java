package eu.binflux.netty.eventhandler;

import eu.binflux.netty.eventhandler.consumer.message.ErrorEvent;
import eu.binflux.netty.exceptions.EventHandlerException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventHandler {

    private final ConcurrentHashMap<Class<?>, List<Consumer<?>>> consumerMap;

    public EventHandler() {
        this.consumerMap = new ConcurrentHashMap<>();
    }

    public <T> void registerConsumer(Class<T> eventClass, Consumer<? super T> consumer) {
        if(!(ConsumerEvent.class.isAssignableFrom(eventClass))) {
            handleEvent(new ErrorEvent(new EventHandlerException("Registered event '" + eventClass.getSimpleName() + "' isn't instance of 'ConsumerEvent.class'!")));
            return;
        }
        List<Consumer<?>> consumerList = new ArrayList<>();
        if (this.consumerMap.containsKey(eventClass))
            consumerList = this.consumerMap.get(eventClass);
        if (!consumerList.contains(consumer))
            consumerList.add(consumer);
        this.consumerMap.put(eventClass, consumerList);
    }

    public boolean hasConsumer(Class<? extends ConsumerEvent> eventClass) {
        return !this.consumerMap.getOrDefault(eventClass, new ArrayList<>()).isEmpty();
    }

    public <T> void handleEvent(T event) {
        if(!(ConsumerEvent.class.isAssignableFrom(event.getClass()))) {
            handleEvent(new ErrorEvent(new EventHandlerException("Handled event '" + event.getClass().getSimpleName() + "' isn't instance of 'ConsumerEvent.class'!")));
            return;
        }
        List<Consumer<?>> unknownConsumerList = this.consumerMap.getOrDefault(event.getClass(), new ArrayList<>());
        if(!unknownConsumerList.isEmpty()) {
            for (Consumer<?> consumer : unknownConsumerList) {
                @SuppressWarnings("unchecked")
                Consumer<? super T> castedConsumer = (Consumer<? super T>) consumer;
                castedConsumer.accept(event);
            }
        }
    }

    public void unregisterAll() {
        this.consumerMap.clear();
    }

}
