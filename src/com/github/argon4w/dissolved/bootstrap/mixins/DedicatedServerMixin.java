package com.github.argon4w.dissolved.bootstrap.mixins;

import com.github.argon4w.dissolved.agent.DissolvedServerBootstrap;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.ServerGUI;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.rcon.thread.RemoteControlListener;
import net.minecraft.server.rcon.thread.RemoteStatusListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {
    @Shadow @Final @Nullable private TextFilter w;

    @Shadow @Nullable private ServerGUI v;

    @Shadow @Nullable private RemoteControlListener t;

    @Shadow @Nullable private RemoteStatusListener r;

    /**
     * @author AR
     * @reason None.
     */
    @Overwrite
    public void g() {
        if (w != null) {
            w.close();
        }

        if (v != null) {
            v.b();
        }

        if (t != null) {
            t.b();
        }

        if (r != null) {
            r.b();
        }

        for (WorldServer worldServer : F()) {
            try {
                worldServer.convertable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DissolvedServerBootstrap.boot(options, getClass().getClassLoader());
    }
}
