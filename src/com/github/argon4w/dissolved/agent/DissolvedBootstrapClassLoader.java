package com.github.argon4w.dissolved.agent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DissolvedBootstrapClassLoader extends URLClassLoader {
    private static final List<String> PASS_THROUGH_PACKAGES = List.of(
            "joptsimple"
    );
    private static final List<String> TRANSFORM_EXCLUDED_PACKAGES = List.of(
            "com.github.argon4w.dissolved",
            "org.spongepowered",
            "org.objectweb"
    );

    private final ClassLoader passthroughClassLoader;
    private final HashMap<String, ClassFileTransformer> classTransformers;
    private final HashMap<String, IUpperClassAccessor> upperClassAccessors;

    public DissolvedBootstrapClassLoader(URL[] URLs, ClassLoader passthroughClassLoader) {
        super(URLs);

        this.passthroughClassLoader = passthroughClassLoader;
        classTransformers = new HashMap<>();
        upperClassAccessors = new HashMap<>();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return exclude(PASS_THROUGH_PACKAGES, name) ? loadOrUpperAccess(name, resolve) : passthroughClassLoader.loadClass(name);
    }

    public Class<?> loadOrUpperAccess(String name, boolean resolve) throws ClassNotFoundException {
        Optional<Class<?>> optional = access(name, c -> super.loadClass(name, resolve));

        for (IUpperClassAccessor accessor : upperClassAccessors.values()) {
            optional = optional.or(() -> access(name, accessor));
        }

        return optional.orElseThrow(() -> new ClassNotFoundException(name));
    }

    public Optional<Class<?>> access(String name, IUpperClassAccessor accessor) {
        try {
            return Optional.of(accessor.accessClass(name));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        try (InputStream inputStream = ensureStream(getResourceAsStream(path))) {
            byte[] bytes = exclude(TRANSFORM_EXCLUDED_PACKAGES, name) ? transformBytes(inputStream.readAllBytes(), name) : inputStream.readAllBytes();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new ClassNotFoundException(e.toString());
        }
    }

    private byte[] transformBytes(byte[] original, String name) throws IllegalClassFormatException {
        for (ClassFileTransformer transformer : classTransformers.values()) {
            original = transformer.transform(this, name, null, null, original);
        }

        return original;
    }

    private boolean exclude(List<String> list, String s) {
        return list.stream().noneMatch(s::startsWith);
    }

    private InputStream ensureStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException();
        }

        return inputStream;
    }

    public void addURL(URL url) {
        super.addURL(url);
    }

    public void addNewTransformer(String id, ClassFileTransformer transformer) {
        classTransformers.put(id, transformer);
    }

    public void addNewUpperClassAccessor(String id, IUpperClassAccessor accessor) {
        upperClassAccessors.put(id, accessor);
    }

    public void removeTransformer(String id) {
        classTransformers.remove(id);
    }

    public void removeUpperClassAccessor(String id) {
        upperClassAccessors.remove(id);
    }
}
