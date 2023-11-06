package com.github.argon4w.dissolved.agent;

import java.util.HashMap;
import java.util.Optional;

public class DissolvedMixinClasses {
    private static final HashMap<String, DissolvedMixinClass> MIXIN_CLASSES = new HashMap<>();

    public static Optional<byte[]> getMixinBytes(String className) {
        return hasMixinsFor(className) ? Optional.of(MIXIN_CLASSES.get(className).mixinBytes()) : Optional.empty();
    }

    public static Optional<byte[]> getOriginalBytes(String className) {
        return hasMixinsFor(className) ? Optional.of(MIXIN_CLASSES.get(className).originalBytes()) : Optional.empty();
    }

    public static boolean hasMixinsFor(String className) {
        return MIXIN_CLASSES.containsKey(className);
    }

    public static void addNewMixin(Class<?> clazz, byte[] originalBytes, byte[] mixinBytes) {
        MIXIN_CLASSES.put(clazz.getName(), new DissolvedMixinClass(clazz.getName(), originalBytes, mixinBytes));
    }
}
