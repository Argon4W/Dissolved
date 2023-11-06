package com.github.argon4w.dissolved.common.mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;

public class Blackboard implements IGlobalPropertyService {
    private final HashMap<String, Key> keys = new HashMap<>();
    private final HashMap<IPropertyKey, Object> blackboard = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return keys.computeIfAbsent(name, Key::new);
    }

    @Override
    public <T> T getProperty(IPropertyKey iPropertyKey) {
        return getProperty(iPropertyKey, null);
    }

    @Override
    public void setProperty(IPropertyKey iPropertyKey, Object o) {
        blackboard.put(iPropertyKey, o);
    }

    @Override
    public <T> T getProperty(IPropertyKey iPropertyKey, T t) {
        return (T) blackboard.getOrDefault(iPropertyKey, t);
    }

    @Override
    public String getPropertyString(IPropertyKey iPropertyKey, String s) {
        return getProperty(iPropertyKey, s);
    }

    record Key(String name) implements IPropertyKey {
        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
