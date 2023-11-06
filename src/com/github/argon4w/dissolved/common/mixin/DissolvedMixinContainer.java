package com.github.argon4w.dissolved.common.mixin;

import com.github.argon4w.dissolved.bootstrap.DissolvedBukkitPlugin;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;

import java.util.Arrays;

public class DissolvedMixinContainer extends ContainerHandleVirtual {
    public DissolvedMixinContainer(String name) {
        super(name);

        add(new DissolvedMixinConfig("mixins.dissolved.bootstrap.json"));
        Arrays.stream(DissolvedBukkitPlugin.getInstance().getServer().getPluginManager().getPlugins())
                .filter(plugin -> plugin.getResource(getPluginMixinName(plugin)) != null)
                .map(plugin -> (IContainerHandle) new DissolvedMixinConfig(getPluginMixinName(plugin)))
                .forEach(this::add);
    }

    public String getPluginMixinName(Plugin plugin) {
        return "mixins.%s.json".formatted(plugin.getName().toLowerCase());
    }
}
