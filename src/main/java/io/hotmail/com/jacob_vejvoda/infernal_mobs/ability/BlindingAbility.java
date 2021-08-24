package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class BlindingAbility extends AbstractAbility {

    private static final PotionEffect BLINDING = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1);

    public BlindingAbility() {
        super("blinding");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (target instanceof Player player) {
            if (RandomNumber.doLottery(20)) {
                applyEffect(player);
            }
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (target instanceof Player player) {
            if (RandomNumber.doLottery(20)) {
                applyEffect(player);
            }
        }
    }

    private void applyEffect(@NotNull Player target) {
        BLINDING.apply(target);
        target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 100, 0.5f);
    }
}
