package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public interface Ability {

    @NotNull String getName();

    default void onSpawn(@NotNull Mob infernalMob) {
    }

    default void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
    }

    default void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
    }

    default void showEffect(@NotNull Mob infernalMob) {
    }

    default void onDamaged(@NotNull Mob infernalMob, double damageAmount) {
    }

    default void onDamaged(@NotNull Mob infernalMob, @NotNull Mob damager, double damageAmount) {
    }

    default void onKilled(@NotNull Mob infernalMob, @NotNull Mob killer) {
    }
}
