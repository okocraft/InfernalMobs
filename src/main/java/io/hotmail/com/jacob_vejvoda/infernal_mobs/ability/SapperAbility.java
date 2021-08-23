package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class SapperAbility extends AbstractAbility {

    private static final PotionEffect SAPPER = new PotionEffect(PotionEffectType.HUNGER, 100, 2);

    SapperAbility() {
        super("sapper");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (target instanceof Player player && InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            applyEffect(player);
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (target instanceof Player player && InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            applyEffect(player);
        }
    }

    private void applyEffect(@NotNull Player target) {
        SAPPER.apply(target);
        target.playSound(target.getLocation(), Sound.ENTITY_CAT_PURR, 200, 2.0f);
    }
}
