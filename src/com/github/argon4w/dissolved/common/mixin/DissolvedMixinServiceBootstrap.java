package com.github.argon4w.dissolved.common.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class DissolvedMixinServiceBootstrap implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "Dissolved";
    }

    @Override
    public String getServiceClassName() {
        return "com.github.argon4w.dissolved.common.mixin.DissolvedMixinService";
    }

    @Override
    public void bootstrap() {

    }
}
