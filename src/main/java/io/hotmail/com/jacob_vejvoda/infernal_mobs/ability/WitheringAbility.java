package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class WitheringAbility extends AbstractAbility {

    private static final PotionEffect WITHERING = new PotionEffect(PotionEffectType.WITHER, 100, 1);

    WitheringAbility() {
        super("withering");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            applyEffect(target);
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            applyEffect(target);
        }
    }

    private void applyEffect(@NotNull Mob target) {
        WITHERING.apply(target);

        if (target instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 100, 1.25f);
        }
    }
}
