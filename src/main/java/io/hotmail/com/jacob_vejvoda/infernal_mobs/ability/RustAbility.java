package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

public class RustAbility extends AbstractAbility {

    RustAbility() {
        super("rust");
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (target instanceof Player player && RandomNumber.doLottery(5)) {
            var mainHand = player.getInventory().getItemInMainHand();

            if (mainHand.getItemMeta() instanceof Damageable damageable) {
                damageItem(mainHand, damageable);
                player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, SoundCategory.MASTER, 100, 0.75f);
                return;
            }

            var offHand = player.getInventory().getItemInOffHand();

            if (offHand.getItemMeta() instanceof Damageable damageable) {
                damageItem(offHand, damageable);
                player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, SoundCategory.MASTER, 100, 0.75f);
            }
        }
    }

    private void damageItem(@NotNull ItemStack item, @NotNull Damageable damageable) {
        var currentDamage = damageable.getDamage();
        damageable.setDamage(currentDamage + 20);

        item.setItemMeta(damageable);
    }
}
