package com.poultry.smartfarm;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Pure JDK Proxy in-memory test helper for Spring Data MongoRepository interfaces.
 * Avoids ByteBuddy dynamic JVM agent attachment inside sandboxed Windows test runners.
 */
public final class InMemoryRepoProxy {

    private InMemoryRepoProxy() {
    }

    @SuppressWarnings("unchecked")
    public static <R, E> R create(Class<R> repoInterface,
                                  Map<String, E> store,
                                  BiFunction<Method, Object[], Object> customHandler) {
        return (R) Proxy.newProxyInstance(
                repoInterface.getClassLoader(),
                new Class<?>[]{repoInterface},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("toString".equals(name)) return repoInterface.getSimpleName() + "-InMemoryProxy";
                    if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                    if ("equals".equals(name)) return proxy == args[0];

                    if (customHandler != null) {
                        Object custom = customHandler.apply(method, args);
                        if (custom != UNHANDLED) {
                            return custom;
                        }
                    }

                    if ("save".equals(name) && args != null && args.length == 1) {
                        E entity = (E) args[0];
                        String id = invokeGetId(entity);
                        if (id == null || id.isBlank()) {
                            id = "id-" + UUID.randomUUID().toString().substring(0, 8);
                            invokeSetId(entity, id);
                        }
                        store.put(id, entity);
                        return entity;
                    }
                    if ("saveAll".equals(name) && args != null && args.length == 1) {
                        Iterable<E> items = (Iterable<E>) args[0];
                        List<E> saved = new ArrayList<>();
                        for (E item : items) {
                            String id = invokeGetId(item);
                            if (id == null || id.isBlank()) {
                                id = "id-" + UUID.randomUUID().toString().substring(0, 8);
                                invokeSetId(item, id);
                            }
                            store.put(id, item);
                            saved.add(item);
                        }
                        return saved;
                    }
                    if ("findById".equals(name) && args != null && args.length == 1) {
                        return Optional.ofNullable(store.get(String.valueOf(args[0])));
                    }
                    if ("findAll".equals(name) || name.startsWith("findAllBy")) {
                        return new ArrayList<>(store.values());
                    }
                    if ("count".equals(name)) {
                        return (long) store.size();
                    }
                    if (name.startsWith("existsBy")) {
                        return false;
                    }
                    if (Optional.class.isAssignableFrom(method.getReturnType())) {
                        return store.values().stream().findFirst();
                    }
                    if (Collection.class.isAssignableFrom(method.getReturnType())) {
                        return new ArrayList<>(store.values());
                    }
                    if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == long.class || method.getReturnType() == Long.class) {
                        return 0L;
                    }
                    return null;
                }
        );
    }

    public static final Object UNHANDLED = new Object();

    public static <R, E> R create(Class<R> repoInterface, Map<String, E> store) {
        return create(repoInterface, store, null);
    }

    public static <R, E> R createEmpty(Class<R> repoInterface) {
        return create(repoInterface, new LinkedHashMap<>(), null);
    }

    private static String invokeGetId(Object entity) {
        try {
            Method m = entity.getClass().getMethod("getId");
            Object v = m.invoke(entity);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void invokeSetId(Object entity, String id) {
        try {
            Method m = entity.getClass().getMethod("setId", String.class);
            m.invoke(entity, id);
        } catch (Exception ignored) {
        }
    }
}
