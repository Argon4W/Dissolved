package com.github.argon4w.dissolved.bootstrap.mixins;

import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public OptionSet options;

    @Shadow public abstract Iterable<WorldServer> F();
}
