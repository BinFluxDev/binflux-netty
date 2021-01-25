package eu.binflux.netty.protocol;

import java.util.*;
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

    public static <O> void addFuture(String futureId, CompletableFuture<O> completableFuture) {
        if (!hasFuture(futureId))
            futureMap.put(futureId, completableFuture);
        completableFuture.whenComplete((t, e) -> removeFuture(futureId));
    }

    @SuppressWarnings("unchecked")
    public static <O> CompletableFuture<O> getFuture(String futureId) {
        return (CompletableFuture<O>) futureMap.getOrDefault(futureId, null);
    }

    public static void removeFuture(String futureId) {
        futureMap.remove(futureId);
    }

    public static <O> Map.Entry<String, CompletableFuture<O>> generateFuture() {
        String futureId = createFutureId();
        CompletableFuture<O> future = new CompletableFuture<>();
        addFuture(futureId, future);
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    public static <O> Map.Entry<String, CompletableFuture<O>> generateFuture(Class<O> classO) {
        return generateFuture();
    }

    public static <O> Map.Entry<String, CompletableFuture<HashMap<String, O>>> generateMapFuture() {
        String futureId = createFutureId();
        CompletableFuture<HashMap<String, O>> future = new CompletableFuture<>();
        addFuture(futureId, future);
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    public static <O> Map.Entry<String, CompletableFuture<HashMap<String, O>>> generateMapFuture(Class<O> classO) {
        return generateMapFuture();
    }

    public static <O> Map.Entry<String, CompletableFuture<HashSet<O>>> generateSetFuture() {
        String futureId = createFutureId();
        CompletableFuture<HashSet<O>> future = new CompletableFuture<>();
        addFuture(futureId, future);
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    public static <O> Map.Entry<String, CompletableFuture<HashSet<O>>> generateSetFuture(Class<O> classO) {
        return generateSetFuture();
    }

    public static <O> Map.Entry<String, CompletableFuture<List<O>>> generateListFuture() {
        String futureId = createFutureId();
        CompletableFuture<List<O>> future = new CompletableFuture<>();
        addFuture(futureId, future);
        return new AbstractMap.SimpleEntry<>(futureId, future);
    }

    public static <O> Map.Entry<String, CompletableFuture<List<O>>> generateListFuture(Class<O> classO) {
        return generateListFuture();
    }

    public static String createFutureId() {
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
