package com.github.argon4w.dissolvedtest.mixins;

import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftServer.class)
public class CraftServerMixin {
    @Inject(method = "getBukkitVersion", at = @At("TAIL"), cancellable = true)
    public void getBukkitVersion(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(cir.getReturnValue() + ", DissolvedTest Enabled.");
    }
}
