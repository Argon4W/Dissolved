package com.github.argon4w.dissolved.common.mixin;

import com.github.argon4w.dissolved.bootstrap.DissolvedBukkitPlugin;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.tools.agent.MixinAgent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;

public record DissolvedBootstrapMixinTransformer(IMixinTransformer classTransformer, DissolvedBukkitPlugin plugin) implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
        return classBeingRedefined != null ? classTransformer.transformClassBytes(null, className.replace('/', '.'), classfileBuffer) : classfileBuffer;
    }
}
