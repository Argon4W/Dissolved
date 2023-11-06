package com.github.argon4w.dissolved.agent;

@FunctionalInterface
public interface IUpperClassAccessor {
    Class<?> accessClass(String name) throws ClassNotFoundException;
}
