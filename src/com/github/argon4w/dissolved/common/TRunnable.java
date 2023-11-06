package com.github.argon4w.dissolved.common;

@FunctionalInterface
public interface TRunnable {
    void run() throws Throwable;

    static Runnable of(TRunnable tRunnable) {
        return () -> {
            try {
                tRunnable.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
    }
}
