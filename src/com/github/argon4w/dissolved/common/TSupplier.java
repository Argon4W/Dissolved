package com.github.argon4w.dissolved.common;

import java.util.function.Supplier;

@FunctionalInterface
public interface TSupplier<R> {
    R get() throws Throwable;

    static <R> Supplier<R> of(TSupplier<R> tSupplier, R fallback) {
        return () -> {
            try {
                return tSupplier.get();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return fallback;
        };
    }
}
