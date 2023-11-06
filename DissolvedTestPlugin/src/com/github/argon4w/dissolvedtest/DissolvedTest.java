package com.github.argon4w.dissolvedtest;

import org.bukkit.plugin.java.JavaPlugin;

public class DissolvedTest extends JavaPlugin {
    @Override
    public void onEnable() {
        super.onEnable();
        System.out.println(getServer().getBukkitVersion());
    }
}
