package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArmouredAbility extends AbstractAbility {

    ArmouredAbility() {
        super("armoured");
    }

    @Override
    public boolean isApplicable(@NotNull Mob mob) {
        return mob instanceof Zombie || mob instanceof Skeleton;
    }

    @Override
    public void onSpawn(@NotNull Mob infernalMob) {
        var equipments = infernalMob.getEquipment();

        if (equipments == null) {
            return;
        }

        Material helmetMaterial;
        Material chestPlateMaterial;
        Material leggingsMaterial;
        Material bootsMaterial;

        switch (RandomNumber.generate(5)) { // each Material has a 20% chance of being chosen.
            case 0 -> {
                helmetMaterial = Material.CHAINMAIL_HELMET;
                chestPlateMaterial = Material.CHAINMAIL_CHESTPLATE;
                leggingsMaterial = Material.CHAINMAIL_LEGGINGS;
                bootsMaterial = Material.CHAINMAIL_BOOTS;
            }
            case 1 -> {
                helmetMaterial = Material.IRON_HELMET;
                chestPlateMaterial = Material.IRON_CHESTPLATE;
                leggingsMaterial = Material.IRON_LEGGINGS;
                bootsMaterial = Material.IRON_BOOTS;
            }
            case 2 -> {
                helmetMaterial = Material.GOLDEN_HELMET;
                chestPlateMaterial = Material.GOLDEN_CHESTPLATE;
                leggingsMaterial = Material.GOLDEN_LEGGINGS;
                bootsMaterial = Material.GOLDEN_BOOTS;
            }
            case 3 -> {
                helmetMaterial = Material.DIAMOND_HELMET;
                chestPlateMaterial = Material.DIAMOND_CHESTPLATE;
                leggingsMaterial = Material.DIAMOND_LEGGINGS;
                bootsMaterial = Material.DIAMOND_BOOTS;
            }
            default -> {
                helmetMaterial = Material.NETHERITE_HELMET;
                chestPlateMaterial = Material.NETHERITE_CHESTPLATE;
                leggingsMaterial = Material.NETHERITE_LEGGINGS;
                bootsMaterial = Material.NETHERITE_BOOTS;
            }
        }

        var helmet = createUnbreakableArmor(helmetMaterial);
        var chestPlate = createUnbreakableArmor(chestPlateMaterial);
        var leggings = createUnbreakableArmor(leggingsMaterial);
        var boots = createUnbreakableArmor(bootsMaterial);

        applyCommonEnchantment(helmet);
        applyCommonEnchantment(chestPlate);
        applyCommonEnchantment(leggings);
        applyCommonEnchantment(boots);
        applyBootsEnchantments(infernalMob, boots);
    }

    private @NotNull ItemStack createUnbreakableArmor(@NotNull Material material) {
        var item = new ItemStack(material, 1);
        var meta = item.getItemMeta();

        if (meta != null) {
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void applyCommonEnchantment(@NotNull ItemStack item) {
        if (RandomNumber.doLottery(3)) {
            item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, RandomNumber.generate(3, 8));
        }

        if (RandomNumber.doLottery(10)) {
            item.addEnchantment(Enchantment.THORNS, RandomNumber.generate(3, true));
        }
    }

    private void applyBootsEnchantments(@NotNull Mob mob, @NotNull ItemStack boots) {
        if (!RandomNumber.doLottery(20)) {
            return;
        }

        var type = mob.getType();
        var world = mob.getWorld();

        if (world.getEnvironment() == World.Environment.NORMAL) {
            if (type == EntityType.ZOMBIE ||
                    type == EntityType.SKELETON ||
                    type == EntityType.STRAY ||
                    type == EntityType.ZOMBIFIED_PIGLIN ||
                    type == EntityType.ZOMBIE_VILLAGER ||
                    type == EntityType.HUSK) {
                boots.addEnchantment(Enchantment.FROST_WALKER, RandomNumber.generate(2, true));
                return;
            }

            if (type == EntityType.DROWNED) {
                // Not an enchantment, but depth_strider has no effect for mobs.
                var speed = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

                if (speed != null) {
                    speed.setBaseValue(0.5);
                }
            }

            return;
        }

        if (world.getBiome(mob.getLocation()) == Biome.SOUL_SAND_VALLEY) {
            if (type == EntityType.SKELETON || type == EntityType.WITHER_SKELETON) {
                boots.addEnchantment(Enchantment.SOUL_SPEED, RandomNumber.generate(3, true));
            }
        }
    }
}
