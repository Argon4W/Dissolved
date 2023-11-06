package com.github.argon4w.dissolved.common.mixin;

import org.spongepowered.asm.launch.platform.container.IContainerHandle;

import java.util.Collection;
import java.util.List;

public record DissolvedMixinConfig(String name) implements IContainerHandle {
    @Override
    public String getAttribute(String s) {
        return s.equals("MixinConfigs") ? name : null;
    }

    @Override
    public Collection<IContainerHandle> getNestedContainers() {
        return List.of();
    }
}
