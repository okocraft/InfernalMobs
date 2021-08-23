package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ConfuseAbility extends AbstractAbility {

    private static final PotionEffect CONFUSION = new PotionEffect(PotionEffectType.CONFUSION, 80, 2);

    ConfuseAbility() {
        super("confuse");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            CONFUSION.apply(target);

            if (target instanceof Player player) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 100, 0.75f);
            }
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (InfernalMobsPlugin.RANDOM.nextInt(20) == 0) {
            CONFUSION.apply(target);

            if (target instanceof Player player) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 100, 0.75f);
            }
        }
    }
}
