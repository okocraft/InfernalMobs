package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class QuicksandAbility extends AbstractAbility {

    private static final PotionEffect SLOW = new PotionEffect(PotionEffectType.SLOW, 100, 1);

    QuicksandAbility() {
        super("quicksand");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (RandomNumber.doLottery(20)) {
            applyEffect(target);
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (RandomNumber.doLottery(20)) {
            applyEffect(target);
        }
    }

    private void applyEffect(@NotNull Mob target) {
        SLOW.apply(target);

        if (target instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_SAND_PLACE, 100, 0.5f);
        }
    }
}
