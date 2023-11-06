package com.github.argon4w.dissolved.common.mixin;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

import java.util.Collection;

public class DissolvedMixinPlatformAgent extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {
    @Override
    public void init() {

    }

    @Override
    public AcceptResult accept(MixinPlatformManager manager, IContainerHandle handle) {
        return AcceptResult.REJECTED;
    }

    @Override
    public String getSideName() {
        return Constants.SIDE_SERVER;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }
}
