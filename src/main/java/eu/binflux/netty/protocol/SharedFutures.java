package eu.binflux.netty.protocol;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SharedFutures {

    private final static ConcurrentHashMap<String, CompletableFuture<?>> futureMap;

    static {
        futureMap = new ConcurrentHashMap<>();
    }

    public static boolean hasFuture(String futureId) {
        return futureMap.containsKey(futureId);
    }

    public static <T> void addFuture(String futureId, CompletableFuture<T> completableFuture) {
        if (!hasFuture(futureId))
            futureMap.put(futureId, completableFuture);
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> getFuture(String futureId) {
        return (CompletableFuture<T>) futureMap.getOrDefault(futureId, null);
    }

    public static void removeFuture(String futureId) {
        futureMap.remove(futureId);
    }

    public static <T> Map.Entry<String, CompletableFuture<T>> generateFuture() {
        String futureId = createFutureId();
        CompletableFuture<T> future = new CompletableFuture<>();
        addFuture(futureId, future);
        future.whenComplete((t, e) -> removeFuture(futureId));
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }


    public static <T> Map.Entry<String, CompletableFuture<HashMap<String, T>>> generateMapFuture() {
        String futureId = createFutureId();
        CompletableFuture<HashMap<String, T>> future = new CompletableFuture<>();
        addFuture(futureId, future);
        future.whenComplete((t, e) -> removeFuture(futureId));
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    public static <T> Map.Entry<String, CompletableFuture<HashSet<T>>> generateSetFuture() {
        String futureId = createFutureId();
        CompletableFuture<HashSet<T>> future = new CompletableFuture<>();
        addFuture(futureId, future);
        future.whenComplete((t, e) -> removeFuture(futureId));
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    private static String createFutureId() {
        int length = 32;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (characters.length() * Math.random());
            stringBuilder.append(characters.charAt(index));
        }
        return stringBuilder.toString();
    }

}
