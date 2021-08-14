package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class EventListener implements Listener {
    private static InfernalMobsPlugin plugin;
    private final Map<String, Long> spawnerMap = new HashMap<>();

    EventListener(InfernalMobsPlugin instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        ItemStack s = plugin.getDiviningStaff();
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (s.getItemMeta() == null || !s.getItemMeta().hasDisplayName() || meta == null || !meta.hasDisplayName()) {
            return;
        }
        if (meta.getDisplayName().equals(s.getItemMeta().getDisplayName())) {
            Entity boss = GUI.getNearbyBoss(player);
            //Make Look At
            if (boss == null) {
                return;
            }
            //Take Powder
            boolean took = false;
            for (ItemStack i : player.getInventory())
                if (i != null && i.getType() == Material.BLAZE_POWDER) {
                    if (i.getAmount() == 1) {
                        player.getInventory().remove(i);
                    } else
                        i.setAmount(i.getAmount() - 1);
                    took = true;
                    break;
                }
            if (!took) {
                player.sendMessage("§cYou need blaze powder to use this!");
                return;
            }

            Vector direction = getVector(player).subtract(getVector(boss)).normalize();

            // Now change the angle
            Location changed = player.getLocation().clone();
            changed.setYaw(180 - toDegree(Math.atan2(direction.getX(), direction.getZ())));
            changed.setPitch(90 - toDegree(Math.acos(direction.getY())));
            player.teleport(changed);
            //Beam
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                //Shoot Beam
                Location eyeLoc = player.getEyeLocation();
                double px = eyeLoc.getX();
                double py = eyeLoc.getY();
                double pz = eyeLoc.getZ();
                double yaw = Math.toRadians(eyeLoc.getYaw() + 90);
                double pitch = Math.toRadians(eyeLoc.getPitch() + 90);
                double x = Math.sin(pitch) * Math.cos(yaw);
                double y = Math.sin(pitch) * Math.sin(yaw);
                double z = Math.cos(pitch);
                for (int j = 1; j <= 10; j++) {
                    for (int i = 1; i <= 10; i++) {
                        Location loc = new Location(player.getWorld(), px + (i * x), py + (i * z), pz + (i * y));
                        beamParticles(loc);
                    }
                }
            }, 5);
        }
    }

    private void beamParticles(Location loc) {
        Optional.ofNullable(loc.getWorld())
                .ifPresent(world -> plugin.displayLavaParticle(world, loc.getX(), loc.getY(), loc.getZ()));
    }

    private float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

    private Vector getVector(Entity entity) {
        if (entity instanceof Player)
            return ((Player) entity).getEyeLocation().toVector();
        else
            return entity.getLocation().toVector();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity ent = e.getRightClicked();
        if (plugin.errorList.contains(p)) {
            plugin.errorList.remove(p);
            p.sendMessage("§6Error report:");

            String name = "";
            try {
                name = ent.getCustomName();
            } catch (Exception ignored) {
            }
            p.sendMessage("§eName: §f" + name);
            p.sendMessage("§eSaved: §f" + plugin.mobSaveFile.getString(ent.getUniqueId().toString()));
            AttributeInstance maxHealth = ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                p.sendMessage("§eHealth: §f" + maxHealth.getValue());
            }
            p.sendMessage("§eInfernal: §f" + ent.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamaged(EntityDamageEvent e) {
        Entity mob = e.getEntity();
        if (plugin.infernalMobMap.containsKey(mob.getUniqueId())) {
            for (Entity entity : mob.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                if ((entity instanceof Player)) {
                    GUI.fixBar((Player) entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.levitateList.contains(p)) {
            p.setAllowFlight(false);
            plugin.levitateList.remove(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightningStrike(LightningStrikeEvent e) {
        for (Entity m : e.getLightning().getNearbyEntities(6.0D, 6.0D, 6.0D)) {
            if (plugin.infernalMobMap.containsKey(m.getUniqueId())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        plugin.giveMobsPowers();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.giveMobsPowers();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity ent : e.getChunk().getEntities()) {
            if (plugin.mobSaveFile.getString(ent.getUniqueId().toString()) != null) {
                plugin.giveMobPowers(ent);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Entity ent : e.getChunk().getEntities()) {
            plugin.infernalMobMap.remove(ent.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (!plugin.infernalMobMap.containsKey(event.getEntity().getUniqueId())) {
            return;
        }

        try {
            Entity attacker = event.getDamager();
            Entity victim = event.getEntity();
            Entity mob;
            if ((attacker instanceof Projectile)) {
                Projectile projectile = (Projectile) event.getDamager();
                if (((projectile.getShooter() instanceof Player)) && (!(victim instanceof Player))) {
                    mob = victim;
                    Player player = (Player) projectile.getShooter();
                    plugin.doEffect(player, mob, false);
                } else if ((!(projectile.getShooter() instanceof Player)) && ((victim instanceof Player))) {
                    mob = (Entity) projectile.getShooter();
                    Player player = (Player) victim;
                    plugin.doEffect(player, mob, true);
                }
            } else if (((attacker instanceof Player)) && (!(victim instanceof Player))) {
                Player player = (Player) attacker;
                mob = victim;
                plugin.doEffect(player, mob, false);
            } else if ((!(attacker instanceof Player)) && ((victim instanceof Player))) {
                Player player = (Player) victim;
                mob = attacker;
                plugin.doEffect(player, mob, true);
            }

            for (Entity entity : victim.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                if ((entity instanceof Player)) {
                    GUI.fixBar((Player) entity);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onMobSpawn(CreatureSpawnEvent event) {
        World world = event.getEntity().getWorld();
        if ((!event.getEntity().hasMetadata("NPC")) && (!event.getEntity().hasMetadata("shopkeeper")) && event.getEntity().getCustomName() == null) {
            if (event.getEntity().getType() == EntityType.ENDER_DRAGON)
                plugin.getLogger().log(Level.INFO, "Detected Entity Spawn: Ender Dragon");
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                Block spawner = plugin.getNearSpawner(event.getEntity().getLocation());
                if (spawner != null) {
                    String name = plugin.getLocationName(spawner.getLocation());
                    if (plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                        if (this.spawnerMap.get(name) == null) {
                            plugin.makeInfernal(event.getEntity(), true);
                            this.spawnerMap.put(name, plugin.serverTime);
                        } else {
                            long startTime = this.spawnerMap.get(name);
                            long endTime = plugin.serverTime;
                            long timePassed = endTime - startTime;
                            int delay = plugin.mobSaveFile.getInt("infernalSpanwers." + name);
                            if (timePassed >= delay) {
                                plugin.makeInfernal(event.getEntity(), true);
                                this.spawnerMap.put(name, plugin.serverTime);
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
            if ((event.getEntity().hasMetadata("NPC")) || (event.getEntity().hasMetadata("shopkeeper"))) {
                return;
            }
            String entName = event.getEntity().getType().name();
            if (((plugin.getConfig().getStringList("mobworlds").contains(world.getName())) || (plugin.getConfig().getStringList("mobworlds").contains("<all>"))) &&
                    (plugin.getConfig().getStringList("enabledmobs").contains(entName)) &&
                    (plugin.getConfig().getInt("naturalSpawnHeight") < event.getEntity().getLocation().getY()) &&
                    (plugin.getConfig().getStringList("enabledSpawnReasons").contains(event.getSpawnReason().toString()))) {
                plugin.makeInfernal(event.getEntity(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.SPAWNER) {
            String name = plugin.getLocationName(e.getBlock().getLocation());
            if (plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                plugin.mobSaveFile.set("infernalSpanwers." + name, null);
                plugin.saveAsync(plugin.mobSaveFile, plugin.saveYML);
                if (e.getPlayer().isOp()) {
                    e.getPlayer().sendMessage("§cYou broke an infernal mob spawner!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            var infernalMob = plugin.infernalMobMap.get(event.getEntity().getUniqueId());

            if (infernalMob == null) {
                return;
            }

            plugin.removeMob(infernalMob);
            List<String> abilities = infernalMob.abilityList;

            if (abilities.contains("explode")) {
                TNTPrimed tnt = (TNTPrimed) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.PRIMED_TNT);
                tnt.setFuseTicks(1);
            }

            boolean isGhost = false;
            EntityEquipment equipment = event.getEntity().getEquipment();
            if (equipment != null) {
                if (equipment.getHelmet() != null) {
                    ItemStack helmet = equipment.getHelmet();
                    if (helmet != null) {
                        ItemMeta meta = helmet.getItemMeta();
                        if (meta != null) {
                            isGhost = meta.getDisplayName().equals("§fGhost Head");
                        }
                    }
                }
            }

            if (abilities.contains("ghost")) {
                plugin.spawnGhost(event.getEntity().getLocation());
            }

            if ((plugin.getConfig().getBoolean("enableDeathMessages")) && ((event.getEntity().getKiller() != null)) && (!isGhost)) {
                Player player = event.getEntity().getKiller();
                if (plugin.getConfig().getList("deathMessages") != null) {
                    List<String> deathMessagesList = plugin.getConfig().getStringList("deathMessages");

                    int index = InfernalMobsPlugin.RANDOM.nextInt(deathMessagesList.size());
                    String deathMessage = deathMessagesList.get(index);
                    String title = plugin.gui.getMobNameTag(event.getEntity());
                    deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                    deathMessage = deathMessage.replace("player", player.getName());

                    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                    if (itemInMainHand.getType() != Material.AIR) {
                        if (itemInMainHand.getItemMeta() != null) {
                            deathMessage = deathMessage.replace("weapon", itemInMainHand.getItemMeta().getDisplayName());
                        }
                    } else {
                        deathMessage = deathMessage.replace("weapon", "fist");
                    }
                    if (event.getEntity().getCustomName() != null) {
                        deathMessage = deathMessage.replace("mob", event.getEntity().getCustomName());
                    } else {
                        deathMessage = deathMessage.replace("mob", title);
                    }
                    Bukkit.broadcastMessage(deathMessage);
                } else {
                    plugin.getLogger().warning("No valid death messages found!");
                }
            }
            if ((plugin.getConfig().getBoolean("enableDrops")) &&
                    ((plugin.getConfig().getBoolean("enableFarmingDrops")) || (event.getEntity().getKiller() != null)) &&
                    ((plugin.getConfig().getBoolean("enableFarmingDrops")) || ((event.getEntity().getKiller() != null)))) {
                Player player = event.getEntity().getKiller();
                if ((player != null) && (player.getGameMode() == GameMode.CREATIVE) && (plugin.getConfig().getBoolean("noCreativeDrops"))) {
                    return;
                }

                var entityName = event.getEntity().getType().getKey().getKey();
                ItemStack drop = plugin.getRandomLoot(player, entityName, abilities.size());
                if (drop != null) {
                    int xpm = plugin.getConfig().getInt("xpMultiplier");
                    int xp = event.getDroppedExp() * xpm;
                    event.setDroppedExp(xp);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}