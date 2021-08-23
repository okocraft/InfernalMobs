package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public interface Ability {

    @NotNull String getName();

    void onSpawn(@NotNull Mob infernalMob);

    void attack(@NotNull Mob infernalMob, @NotNull Mob target);

    void counterattack(@NotNull Mob infernalMob, @NotNull Mob target);

    void showEffect(@NotNull Mob infernalMob);

    void onDamaged(@NotNull Mob infernalMob, @NotNull Mob damager);

    void onKilled(@NotNull Mob infernalMob, @NotNull Mob killer);
}
