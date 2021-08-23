package io.hotmail.com.jacob_vejvoda.infernal_mobs.util;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class NamespaceFactory {

    private static Plugin PLUGIN;

    public static void init(@NotNull InfernalMobsPlugin plugin) {
        if (PLUGIN == null) {
            PLUGIN = plugin;
        }
    }

    @Contract("_ -> new")
    public static @NotNull NamespacedKey create(@NotNull String value) {
        return new NamespacedKey(PLUGIN, value);
    }

}
