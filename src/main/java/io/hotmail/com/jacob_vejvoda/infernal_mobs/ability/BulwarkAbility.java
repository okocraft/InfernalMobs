package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalMobsPlugin;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class BulwarkAbility extends AbstractAbility {

    private static final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 2);

    public BulwarkAbility() {
        super("bulwark");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (RandomNumber.doLottery(20)) {
            applyEffect(infernalMob);
        }
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (RandomNumber.doLottery(20)) {
            applyEffect(infernalMob);
        }
    }

    private void applyEffect(@NotNull Mob target) {
        RESISTANCE.apply(target);

        var world = target.getWorld();
        var location = target.getLocation();

        world.spawnParticle(Particle.BLOCK_DUST, location.add(0, 1, 0), 100, 0.5, 0.5, 0.5);
    }
}
