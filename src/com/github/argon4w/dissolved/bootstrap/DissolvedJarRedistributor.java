package com.github.argon4w.dissolved.bootstrap;

import com.google.common.reflect.ClassPath;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class DissolvedJarRedistributor {
    private final JarOutputStream jarOutputStream;
    private final ClassLoader resourceGatherContext;

    public DissolvedJarRedistributor(File file, Manifest manifest) throws IOException {
        this(file, manifest, DissolvedJarRedistributor.class.getClassLoader());
    }

    public DissolvedJarRedistributor(File file, Manifest manifest, ClassLoader context) throws IOException {
        jarOutputStream = new JarOutputStream(new FileOutputStream(file), manifest);
        resourceGatherContext = context;
    }

    public void gatherClass(String className) throws IOException {
        gatherResource(className.replace('.', '/').concat(".class"));
    }

    public void gatherPackage(String packageName) throws IOException {
        gatherPath(packageName.replace('.', '/'));
    }

    public void gatherPath(String path) throws IOException {
        for (ClassPath.ResourceInfo resourceInfo : ClassPath.from(resourceGatherContext).getResources()) {
            String name = resourceInfo.getResourceName();
            if (name.startsWith(path)) {
                gatherResource(name);
            }
        }
    }

    public void gatherResource(String resource) throws IOException {
        try (InputStream inputStream = ensureStream(resourceGatherContext.getResourceAsStream(resource))) {
            gatherResource(resource, inputStream.readAllBytes());
        }
    }

    public void gatherResource(String target, byte[] bytes) throws IOException {
        jarOutputStream.putNextEntry(new JarEntry(target));
        jarOutputStream.write(bytes);
        jarOutputStream.closeEntry();
    }

    public void redistribute() throws IOException {
        jarOutputStream.flush();
        jarOutputStream.close();
    }

    public InputStream ensureStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException();
        }

        return inputStream;
    }
}
