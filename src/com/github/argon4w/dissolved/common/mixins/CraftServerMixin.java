package com.github.argon4w.dissolved.common.mixins;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.logging.Logger;

@Mixin(CraftServer.class)
public abstract class CraftServerMixin {
    @Shadow public abstract Logger getLogger();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(DedicatedServer console, PlayerList playerList, CallbackInfo ci) {
        getLogger().info("Successfully injected CraftServer.");
    }

    @Inject(method = "getName", at = @At("TAIL"), cancellable = true)
    public void getName(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Dissolved(%s)".formatted(cir.getReturnValue()));
    }
}
