package com.github.argon4w.dissolved.common;

import java.util.function.Consumer;

@FunctionalInterface
public interface TConsumer<T> {
    void accept(T t) throws Throwable;

    static <T> Consumer<T> of(TConsumer<T> tConsumer) {
        return t -> {
            try {
                tConsumer.accept(t);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
    }
}
