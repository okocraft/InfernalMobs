package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class LifeSteal extends AbstractAbility {

    LifeSteal() {
        super("lifesteal");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (RandomNumber.doLottery(10)) {
            target.damage(2.0, infernalMob);

            var current = infernalMob.getHealth();
            var newValue = current + 20.0;
            var maxHealth = infernalMob.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (maxHealth != null && maxHealth.getBaseValue() < newValue) {
                target.setHealth(maxHealth.getBaseValue());
            } else {
                target.setHealth(newValue);
            }

            var world = infernalMob.getWorld();
            var location = infernalMob.getLocation();

            world.spawnParticle(Particle.VILLAGER_HAPPY, location.add(0.0, 0.5, 0.0), 50, 1.0, 1.0, 1.0);
            world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 100, 2.0f);
        }
    }
}
