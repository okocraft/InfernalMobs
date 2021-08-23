package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.NamespaceFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class OneUpAbility extends AbstractAbility {

    private final NamespacedKey namespacedKey;

    public OneUpAbility() {
        super("1up");

        this.namespacedKey = NamespaceFactory.create(getName());
    }

    @Override
    public void onSpawn(@NotNull Mob infernalMob) {
        int current = getLives(infernalMob);
        setLives(infernalMob, current + 1);
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
    }

    @Override
    public void showEffect(@NotNull Mob infernalMob) {
    }

    @Override
    public void onDamaged(@NotNull Mob infernalMob) {
        if (5 < infernalMob.getHealth()) {
            return;
        }

        var lives = getLives(infernalMob);

        if (lives < 2) {
            return;
        }

        var maxHealth = infernalMob.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (maxHealth == null) {
            return;
        }

        var maxValue = maxHealth.getValue();

        infernalMob.setHealth(maxValue);
        setLives(infernalMob, lives - 1);

        var world = infernalMob.getWorld();
        var location = infernalMob.getLocation();

        world.spawnParticle(Particle.HEART, location, 5, 0.5, 0.5, 0.5);
        world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 100, 1.5f);
    }

    @Override
    public void onDamaged(@NotNull Mob infernalMob, @NotNull Mob damager) {
    }

    @Override
    public void onKilled(@NotNull Mob infernalMob, @NotNull Mob killer) {
    }

    private int getLives(@NotNull Mob infernalMob) {
        return infernalMob.getPersistentDataContainer().getOrDefault(namespacedKey, PersistentDataType.INTEGER, 1);
    }

    private void setLives(@NotNull Mob infernalMob, int value) {
        infernalMob.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
    }
}
