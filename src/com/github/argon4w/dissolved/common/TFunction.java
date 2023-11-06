package com.github.argon4w.dissolved.common;

import java.util.function.Function;

@FunctionalInterface
public interface TFunction<T, R> {
    R apply(T t) throws Throwable;

    static <T, R> Function<T, R> of(TFunction<T, R> tFunction, R fallback) {
        return t -> {
            try {
                return tFunction.apply(t);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return fallback;
        };
    }
}
