package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.enchantments.Enchantment;

class LevelledEnchantment {
    public final Enchantment getEnchantment;
    public final int getLevel;

    LevelledEnchantment(Enchantment enchantment, int level) {
        this.getEnchantment = enchantment;
        this.getLevel = level;
    }
}
