package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.RandomNumber;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;

public class BerserkAbility extends AbstractAbility {

    BerserkAbility() {
        super("berserk");
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        tryBerserk(infernalMob, target);
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        tryBerserk(infernalMob, target);
    }

    private void tryBerserk(@NotNull Mob infernalMob, @NotNull Mob target) {
        if (!(target instanceof Player player) || !RandomNumber.doLottery(10)) {
            return;
        }

        var item = player.getInventory().getItemInMainHand();
        boolean isMainHand = true;

        if (item.getType().isAir()) {
            var offHand = player.getInventory().getItemInOffHand();

            if (offHand.getType().isAir()) {
                return;
            }

            item = offHand;
            isMainHand = false;
        }

        if (infernalMob instanceof Zombie || infernalMob instanceof Skeleton) {
            var equipments = infernalMob.getEquipment();

            if (equipments == null) {
                return;
            }

            equipments.setItemInMainHand(item);
        } else {
            target.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        if (isMainHand) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }
    }
}
