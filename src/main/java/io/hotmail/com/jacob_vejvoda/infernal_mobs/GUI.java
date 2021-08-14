package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class GUI implements Listener {
    private static final Map<String, Scoreboard> playerScoreBoard = new HashMap<>();
    private static final Map<Entity, BossBar> bossBars = new HashMap<>();

    private static InfernalMobsPlugin plugin;

    GUI(InfernalMobsPlugin instance) {
        plugin = instance;
    }

    public static Entity getNearbyBoss(Player p) {
        double distance = 26.0D;
        for (InfernalMob m : plugin.infernalList) {
            if (m.entity.getWorld().equals(p.getWorld())) {
                Entity boss = m.entity;
                if (p.getLocation().distance(boss.getLocation()) < distance) {
                    return boss;
                }
            }
        }
        return null;
    }

    static void fixBar(Player p) {
        //System.out.println("fixBar");
        Entity b = getNearbyBoss(p);
        if (b != null) {
            System.out.println("Dead: " + b.isDead());
            System.out.println("HP: " + ((Damageable) b).getHealth());
            if (b.isDead() || ((Damageable) b).getHealth() <= 0) {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    Optional.ofNullable(bossBars.remove(b)).ifPresent(BossBar::removeAll);
                }
                int mobIndex = plugin.idSearch(b.getUniqueId());
                try {
                    if (mobIndex != -1)
                        plugin.removeMob(mobIndex);
                } catch (IOException ignored) {
                }
                clearInfo(p);
            } else {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    showBossBar(p, b);
                }
                if (plugin.getConfig().getBoolean("enableScoreBoard")) {
                    fixScoreboard(p, b, plugin.findMobAbilities(b.getUniqueId()));
                }
            }
        } else
            clearInfo(p);
    }

    private static void showBossBar(Player p, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        var mob = (LivingEntity) entity;

        List<String> oldMobAbilityList = plugin.findMobAbilities(mob.getUniqueId());
        String title = plugin.getConfig().getString("bossBarsName", "&fLevel <powers> &fInfernal <mobName>");
        String mobName = Objects.requireNonNullElse(mob.getType().getKey().getKey(), mob.getType().name()).replace("_", " ");

        if (mob.getType() == EntityType.WITHER_SKELETON) {
            mobName = "WitherSkeleton";
        }

        String prefix = plugin.getConfig().getString("namePrefix", "&fInfernal");
        if (plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null) {
            prefix = plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size(), "");
        }
        title = title.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
        title = title.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
        title = title.replace("<mobLevel>", oldMobAbilityList.size() + "");
        String abilities = plugin.generateString(5, oldMobAbilityList);
        int count = 4;
        try {
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
                if (count <= 0) {
                    break;
                }
            } while (title.length() + abilities.length() + mobName.length() > 64);
        } catch (Exception x) {
            System.out.println("showBossBar error: ");
            x.printStackTrace();
        }
        title = title.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
        title = ChatColor.translateAlternateColorCodes('&', title);

        if (!bossBars.containsKey(mob)) {
            BarColor bc = BarColor.valueOf(plugin.getConfig().getString("bossBarSettings.defaultColor"));
            BarStyle bs = BarStyle.valueOf(plugin.getConfig().getString("bossBarSettings.defaultStyle"));
            //Per Level Settings
            String lc = plugin.getConfig().getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".color");
            if (lc != null)
                bc = BarColor.valueOf(lc);
            String ls = plugin.getConfig().getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".style");
            if (ls != null)
                bs = BarStyle.valueOf(ls);

            var entityName = mob.getType().getKey().getKey();
            //Per InfernalMob Settings
            String mc = plugin.getConfig().getString("bossBarSettings.perMob." + entityName + ".color");
            if (mc != null)
                bc = BarColor.valueOf(mc);
            String ms = plugin.getConfig().getString("bossBarSettings.perMob." + entityName + ".style");
            if (ms != null)
                bs = BarStyle.valueOf(ms);
            BossBar bar = Bukkit.createBossBar(title, bc, bs, BarFlag.CREATE_FOG);
            bar.setVisible(true);
            bossBars.put(mob, bar);
        }

        if (!bossBars.get(mob).getPlayers().contains(p)) {
            bossBars.get(mob).addPlayer(p);
        }

        var maxHealthAttribute = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (maxHealthAttribute != null) {
            double health = mob.getHealth();
            double maxHealth = maxHealthAttribute.getBaseValue();
            bossBars.get(mob).setProgress(health / maxHealth);
        }
    }

    private static void clearInfo(Player player) {
        if (plugin.getConfig().getBoolean("enableBossBar")) {
            //BossBarAPI.removeBar(player);
            for (Entry<Entity, BossBar> hm : bossBars.entrySet())
                if (hm.getValue().getPlayers().contains(player))
                    hm.getValue().removePlayer(player);
        }
        if (plugin.getConfig().getBoolean("enableScoreBoard")) {
            player.getScoreboard().resetScores(player.getName());
            Objective sidebarObjective = player.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
            if (sidebarObjective != null) {
                sidebarObjective.unregister();
            }
        }
    }

    private static void fixScoreboard(Player player, Entity e, List<String> abilityList) {
        if (!plugin.getConfig().getBoolean("enableScoreBoard") || !(e instanceof LivingEntity)) {
            return;
        }

        if (playerScoreBoard.get(player.getName()) == null) {
            //System.out.println("Creating ScoreBoard");
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) {
                return;
            }
            Scoreboard board = manager.getNewScoreboard();
            playerScoreBoard.put(player.getName(), board);
        }


        Scoreboard board = playerScoreBoard.get(player.getName());
        Objective objective =
                Optional.ofNullable(board.getObjective(DisplaySlot.SIDEBAR))
                        .orElseGet(() -> {
                            var temp = board.registerNewObjective(player.getName(), "dummy", "dummy");
                            temp.setDisplaySlot(DisplaySlot.SIDEBAR);
                            return temp;
                        });


        objective.setDisplayName(e.getType().getKey().getKey());

        board.getEntries().forEach(board::resetScores);

        int score = 1;

        for (String ability : abilityList) {
            objective.getScore("§r" + ability).setScore(score);
            score = score + 1;
        }

        objective.getScore("§e§lAbilities:").setScore(score);

        var livingEntity = (LivingEntity) e;
        var maxHealthAttribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (plugin.getConfig().getBoolean("showHealthOnScoreBoard") && maxHealthAttribute != null) {
            score = score + 1;

            double roundOff = Math.round(livingEntity.getHealth() * 100.0) / 100.0;
            objective.getScore(roundOff + "/" + maxHealthAttribute.getBaseValue()).setScore(score);

            score = score + 1;
            objective.getScore("§e§lHealth:").setScore(score);
        }

        player.getScoreboard();
        if (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null ||
                !objective.equals(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR))) {
            player.setScoreboard(board);
        }
    }

    public void setName(Entity ent) {
        try {
            //System.out.println("SN1 " + ent);
            if (plugin.getConfig().getInt("nameTagsLevel") != 0) {
                String title = getMobNameTag(ent);
                ent.setCustomName(title);
                if (plugin.getConfig().getInt("nameTagsLevel") == 2) {
                    ent.setCustomNameVisible(true);
                }
            }
        } catch (Exception x) {
            System.out.println("Error in setName: ");
            x.printStackTrace();
        }
    }


    public String getMobNameTag(Entity entity) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(entity.getUniqueId());
        try {
            String title = plugin.getConfig().getString("nameTagsName", "&fInfernal <mobName>");
            String mobName = entity.getType().getKey().getKey().replace("_", " ");

            title = title.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
            title = title.replace("<mobLevel>", "" + oldMobAbilityList.size());
            String abilities;
            int count = 4;
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
            } while ((title.length() + abilities.length() + mobName.length()) > 64);
            title = title.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
            //Prefix
            String prefix = plugin.getConfig().getString("namePrefix", "");
            if (plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null)
                prefix = plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size(), "");
            title = title.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
            title = ChatColor.translateAlternateColorCodes('&', title);
            return title;
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x.getMessage());
            x.printStackTrace();
            return null;
        }
    }
}