package com.github.argon4w.dissolved.common.mixin;

import com.github.argon4w.dissolved.bootstrap.DissolvedBukkitPlugin;
import com.github.argon4w.dissolved.common.TFunction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.Main;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.transformers.MixinClassReader;
import org.spongepowered.asm.util.IConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class DissolvedMixinService extends MixinServiceAbstract implements IClassProvider, IClassBytecodeProvider {
    private IMixinTransformer transformer;
    private ClassFileTransformer agentTransformer;
    private IConsumer<MixinEnvironment.Phase> phaseConsumer;


    @Override
    public String getName() {
        return "Dissolved";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
        this.phaseConsumer = phaseConsumer;
    }

    public void startup() {
        phaseConsumer.accept(MixinEnvironment.Phase.DEFAULT);
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) {
            transformer = factory.createTransformer();
        }

        super.offer(internal);
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("com.github.argon4w.dissolved.common.mixin.DissolvedMixinPlatformAgent");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new DissolvedMixinContainer(getName());
    }

    @Override
    public URL[] getClassPath() {
        return getClass().getClassLoader() instanceof URLClassLoader urlClassLoader ? urlClassLoader.getURLs() : new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @Override
    public Class<?> findClass(String name, boolean initialized) throws ClassNotFoundException {
        return mapAllPlugins(TFunction.of(plugin -> Class.forName(name, initialized, plugin.getClass().getClassLoader()), null))
                .findFirst()
                .orElseThrow(() -> new ClassNotFoundException(name));
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialized) throws ClassNotFoundException {
        return Class.forName(name, initialized, this.getClass().getClassLoader());
    }

    @Override
    public ClassNode getClassNode(String name) throws IOException {
        return getClassNode(name, true);
    }

    public InputStream getResourceAsStream(String name) {
        return mapAllPlugins(plugin -> plugin.getClass().getClassLoader().getResourceAsStream(name))
                .findFirst()
                .orElse(Main.class.getClassLoader().getResourceAsStream(name));
    }

    public byte[] getResourceBytes(String name) throws IOException {
        try (InputStream stream = getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException();
            }

            return stream.readAllBytes();
        }
    }

    @Override
    public ClassNode getClassNode(String name, boolean initialized) throws IOException {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new MixinClassReader(getResourceBytes(name.replace('.', '/').concat(".class")), name);
        reader.accept(classNode, 8);

        return classNode;
    }

    public <T> Stream<T>  mapAllPlugins(Function<Plugin, T> mapper) {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(mapper)
                .filter(Objects::nonNull);
    }

    public void attach(Instrumentation instrumentation, ClassFileTransformer transformer) {
        agentTransformer = transformer;
        instrumentation.addTransformer(agentTransformer, true);
    }

    public void detach(Instrumentation instrumentation) {
        instrumentation.removeTransformer(agentTransformer);
    }

    public IMixinTransformer getTransformer() {
        return transformer;
    }
}
