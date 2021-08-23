package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.ability.Abilities;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.util.NamespaceFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InfernalMobsPlugin extends JavaPlugin implements Listener {

    private static final Map<String, PotionEffect> EFFECT_MAP =
            Map.of("cloaked", new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1),
                    "armoured", new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1),
                    "sprint", new PotionEffect(PotionEffectType.SPEED, 40, 1),
                    "molten", new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1));
    public static final Random RANDOM = new Random();

    GUI gui;
    long serverTime = 0L;
    final Map<UUID, InfernalMob> infernalMobMap = new LinkedHashMap<>();
    private File lootYML = new File(getDataFolder(), "loot.yml");
    final File saveYML = new File(getDataFolder(), "save.yml");
    private YamlConfiguration lootFile = YamlConfiguration.loadConfiguration(lootYML);
    final YamlConfiguration mobSaveFile = YamlConfiguration.loadConfiguration(saveYML);
    private final HashMap<Entity, Entity> mountList = new HashMap<>();
    final List<Player> errorList = new ArrayList<>();
    final List<Player> levitateList = new ArrayList<>();

    public void onEnable() {
        NamespaceFactory.init(this);

        //Register Events
        getServer().getPluginManager().registerEvents(this, this);
        EventListener events = new EventListener(this);
        getServer().getPluginManager().registerEvents(events, this);
        gui = new GUI(this);
        getServer().getPluginManager().registerEvents(gui, this);
        getLogger().log(Level.INFO, "Registered Events.");
        //Folder
        File dir = new File(getDataFolder().getParentFile().getPath(), getName());
        if (!dir.exists())
            dir.mkdir();
        //Old config check
        if (new File(getDataFolder(), "config.yml").exists()) {
            String configVersion = getConfig().getString("configVersion");
            if (configVersion == null) {
                getLogger().log(Level.INFO, "No config version found!");
                getConfig().set("configVersion", Bukkit.getVersion().split(":")[1].replace(")", "").trim());
                saveConfig();
            } else if (!Bukkit.getVersion().contains(configVersion)) {
                getLogger().info(Bukkit.getVersion() + " contains " + getConfig().getString("configVersion"));
                getLogger().info("Old config found, deleting!");
                new File(getDataFolder() + File.separator + "config.yml").delete();
            }
        }
        //Register Config

        String configVersion = null;
        if (Bukkit.getVersion().contains("1.13") ||
                Bukkit.getVersion().contains("1.14") ||
                Bukkit.getVersion().contains("1.15")) {
            configVersion = "1_15";
        }
        if (Bukkit.getVersion().contains("1.16") ||
                Bukkit.getVersion().contains("1.17")) {
            configVersion = "1_17";
        }

        if (!new File(getDataFolder(), "config.yml").exists()) {
            //saveDefaultConfig();
            getLogger().log(Level.INFO, "No config.yml found, generating...");
            //Generate Config
            boolean generatedConfig = false;
            //for(String version : Arrays.asList("1.12","1.11","1.10","1.9","1.8"))

            if (configVersion != null) {
                saveResource(configVersion + "_config.yml", false);
                new File(getDataFolder(), configVersion + "_config.yml").renameTo(new File(getDataFolder(), "config.yml"));
                getConfig().set("configVersion", Bukkit.getVersion().split(":")[1].replace(")", "").trim());
                getConfig().options().header(
                        "Chance is the chance that a mob will not be infernal, the lower the number the higher the chance. (min 1)\n" +
                                "Enabledworlds are the worlds that infernal mobs can spawn in.\n" +
                                "Enabledmobs are the mobs that can become infernal.\n" +
                                "Loot is the items that are dropped when an infernal mob dies. (You can have up to 64)\n" +
                                "Item is the item, Amount is the amount, Durability is how damaged it will be (0 is undamaged).\n" +
                                "nameTagsLevel is the visibility level of the name tags, 0 = no tag, \n" +
                                "1 = tag shown when your looking at the mob, 2 = tag always shown.\n" +
                                "Note, if you have name tags set to 0, on server restart all infernal mobs will turn normal.\n" +
                                "If you want to enable the boss bar you must have BarAPI on your server.\n" +
                                "nameTagsName and bossBarsName have these special tags: <mobLevel> = the amount of powers the boss has.\n" +
                                "<abilities> = A list of about 3-5 (whatever can fit) names of abilities the boss has.\n" +
                                "<mobName> = Name of the mob, so if the mob is a creeper the mobName will be \"Creeper\".");
                saveConfig();
                getLogger().log(Level.INFO, "Config successfully generated!");
                generatedConfig = true;
            }

            if (!generatedConfig) {
                getLogger().log(Level.SEVERE, "No config available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
            reloadConfig();
        }
        //Register Loots
        if (!lootYML.exists()) {
            getLogger().log(Level.INFO, "No loot.yml found, generating...");
            //Generate Config
            boolean generatedConfig = false;

            if (configVersion != null) {
                saveResource(configVersion + "loot.yml", false);
                new File(getDataFolder(), configVersion + "loot.yml").renameTo(new File(getDataFolder(), "loot.yml"));
                getLogger().log(Level.INFO, Bukkit.getVersion() + " Loot successfully generated!");
                generatedConfig = true;
            }

            if (!generatedConfig) {
                getLogger().log(Level.SEVERE, "No loot available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
            reloadLoot();
        }
        //Register Save File
        if (!saveYML.exists()) {
            try {
                saveYML.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        applyEffect();
        giveMobsPowers();
        showEffect();
        addRecipes();
    }

    private void scoreCheck() {
        for (Player p : getServer().getOnlinePlayers()) {
            GUI.fixBar(p);
        }

        for (var entry : Map.copyOf(mountList).entrySet()) {
            var rider = entry.getKey();
            var mountedMob = entry.getValue();

            if (rider == null || rider.isDead()) {
                mountList.remove(rider);
                return;
            }

            if (mountedMob.isDead() && rider instanceof LivingEntity) {
                String fate = getConfig().getString("mountFate", "nothing");
                if (fate.equals("death")) {
                    ((LivingEntity) rider).damage(999999999);
                } else if (fate.equals("removal")) {
                    rider.remove();
                }

                mountList.remove(rider);
            }
        }
    }

    void giveMobsPowers() {
        for (String id : mobSaveFile.getKeys(false)) {
            UUID uuid;

            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return;
            }

            Optional.ofNullable(getServer().getEntity(uuid)).ifPresent(this::giveMobPowers);
        }
    }

    void giveMobPowers(Entity ent) {
        UUID id = ent.getUniqueId();

        if (infernalMobMap.containsKey(ent.getUniqueId())) {
            return;
        }

        List<String> aList = null;

        for (MetadataValue v : ent.getMetadata("infernalMetadata")) {
            aList = Arrays.asList(v.asString().split(","));
        }

        if (aList == null) {
            var powers = mobSaveFile.getString(ent.getUniqueId().toString());
            if (powers != null) {
                aList = Arrays.asList(powers.split(","));
                ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, powers));
            } else {
                aList = getAbilitiesAmount(ent);
            }
        }
        InfernalMob newMob = new InfernalMob(ent, id, true, aList, 1, getEffect());

        if (aList.contains("flying")) {
            makeFly(ent);
        }
        addNewInfernalMobToMap(newMob);
    }

    void makeInfernal(final Entity e, final boolean fixed) {
        if (!fixed && e instanceof Ageable && !((Ageable) e).isAdult() &&
                getConfig().getStringList("disabledBabyMobs").contains(e.getType().name())) {
            return;
        }

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            var uuid = e.getUniqueId();
            var entityType = e.getType().name();

            if (e.isDead() || !e.isValid() ||
                    (!getConfig().getStringList("enabledmobs").contains(entityType) && infernalMobMap.containsKey(uuid))) {
                return;
            }

            if (!fixed) {
                int max;
                int mc = getConfig().getInt("mobChances." + entityType);

                if (mc > 0) {
                    max = mc;
                } else {
                    max = getConfig().getInt("chance");
                }

                if (max != 1 && RANDOM.nextInt(max) != 0) {
                    return;
                }
            }

            var abilities = getAbilitiesAmount(e);

            var levelChance = getConfig().getInt("levelChance." + abilities.size(), 0);

            if (1 < levelChance) {
                if (RANDOM.nextInt(levelChance) != 0) {
                    return;
                }
            }

            InfernalMob newMob = new InfernalMob(e, uuid, true, abilities, 1, getEffect());

            //fire event
            InfernalSpawnEvent infernalEvent = new InfernalSpawnEvent(e, newMob);
            getServer().getPluginManager().callEvent(infernalEvent);
            if (infernalEvent.isCancelled()) {
                return;
            }

            if (abilities.contains("1up")) {
                Abilities.ONE_UP.onSpawn((Mob) e); // TODO: change LivingEntity to Mob
            }

            addNewInfernalMobToMap(newMob);

            if (abilities.contains("flying")) {
                makeFly(e);
            }

            gui.setName(e);
            giveMobGear(newMob, true);
            addHealth(newMob, abilities);

            if (!getConfig().getBoolean("enableSpawnMessages")) {
                return;
            }

            var spawnMessages = getConfig().getStringList("spawnMessages");
            int index = RANDOM.nextInt(spawnMessages.size());

            var message =
                    ChatColor.translateAlternateColorCodes('&', spawnMessages.get(index))
                            .replace("mob", e.getName());

            var radius = getConfig().getInt("spawnMessageRadius");

            switch (radius) {
                case -1:
                    e.getWorld().getPlayers().forEach(p -> p.sendMessage(message));
                    break;
                case -2:
                    getServer().broadcastMessage(message);
                    break;
                default:
                    if (0 < radius) {
                        e.getNearbyEntities(radius, radius, radius).forEach(entity -> entity.sendMessage(message));
                    }
            }
        }, 10L);
    }

    private void addHealth(InfernalMob infernalMob, List<String> powerList) {
        if (!(infernalMob.entity instanceof LivingEntity)) {
            return;
        }

        var ent = (LivingEntity) infernalMob.entity;

        AttributeInstance attributeMaxHealth = ent.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (attributeMaxHealth == null) {
            return;
        }

        double maxHealth = attributeMaxHealth.getBaseValue();

        float setHealth;

        if (getConfig().getBoolean("healthByPower")) {
            setHealth = (float) (maxHealth * infernalMob.abilityList.size());
        } else {
            if (getConfig().getBoolean("healthByDistance")) {
                double distance = ent.getWorld().getSpawnLocation().distance(ent.getLocation());
                int addDistance = getConfig().getInt("addDistance");
                int multipier = distance <= addDistance ? 1 : (int) distance / addDistance;

                setHealth = (float) multipier * getConfig().getInt("healthToAdd");
            } else {
                setHealth = (float) maxHealth * getConfig().getInt("healthMultiplier");
            }
        }

        if (1.0f <= setHealth) {
            attributeMaxHealth.setBaseValue(setHealth);
            ent.setHealth(setHealth);
        }

        String list = getPowerString(powerList);
        ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
        mobSaveFile.set(ent.getUniqueId().toString(), list);
        saveAsync(mobSaveFile, saveYML);
    }

    private String getPowerString(List<String> powerList) {
        return String.join(",", powerList);
    }

    void removeMob(InfernalMob mob) {
        removeMob(mob, true);
    }

    void removeMob(InfernalMob mob, boolean save) {
        if (infernalMobMap.remove(mob.id) != null) {
            mobSaveFile.set(mob.id.toString(), null);

            if (save) {
                saveAsync(mobSaveFile, saveYML);
            }
        }
    }

    void spawnGhost(Location l) {
        boolean evil = RANDOM.nextInt(3) == 1;
        Zombie g = (Zombie) l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
        g.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 199999980, 1));
        g.setCanPickupItems(false);

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack skull;
        if (evil) {
            skull = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
            dye(chest, Color.BLACK);
        } else {
            skull = new ItemStack(Material.SKELETON_SKULL, 1);
            dye(chest, Color.WHITE);
        }
        chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, RANDOM.nextInt(10) + 1);
        ItemMeta m = Objects.requireNonNull(skull.getItemMeta());
        m.setDisplayName("§fGhost Head");
        skull.setItemMeta(m);
        EntityEquipment equipment = g.getEquipment();
        if (equipment == null) {
            return;
        }
        g.getEquipment().setHelmet(skull);
        g.getEquipment().setChestplate(chest);
        g.getEquipment().setHelmetDropChance(0.0F);
        g.getEquipment().setChestplateDropChance(0.0F);

        if (RANDOM.nextInt(4) == 0) {
            g.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_HOE, 1));
            g.getEquipment().setItemInMainHandDropChance(0.0F);
        }
        ghostMove(g);

        List<String> aList = new ArrayList<>();
        aList.add("ender");
        if (evil) {
            aList.add("necromancer");
            aList.add("withering");
            aList.add("blinding");
        } else {
            aList.add("ghastly");
            aList.add("sapper");
            aList.add("confusing");
        }
        InfernalMob newMob;
        if (evil) {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "smoke:2:12");
        } else {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "cloud:0:8");
        }

        addNewInfernalMobToMap(newMob);
    }

    private void ghostMove(final Entity g) {
        if (g.isDead()) {
            return;
        }
        Vector v = g.getLocation().getDirection().multiply(0.3D);
        g.setVelocity(v);

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                ghostMove(g);
            } catch (Exception ignored) {
            }
        }, 2L);
    }

    private void dye(ItemStack item, Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
    }

    private boolean mobPowerLevelFine(int lootId, int mobPowers) {
        int min = 0;
        int max = 99;
        if (lootFile.getString("loot." + lootId + ".powersMin") != null) {
            min = lootFile.getInt("loot." + lootId + ".powersMin");
        }
        if (lootFile.getString("loot." + lootId + ".powersMax") != null)
            max = lootFile.getInt("loot." + lootId + ".powersMax");
        if (getConfig().getBoolean("debug"))
            getLogger().log(Level.INFO, "Loot " + lootId + " min = " + min + " and max = " + max);
        return (mobPowers >= min) && (mobPowers <= max);
    }

    ItemStack getRandomLoot(Player player, String mob, int powers) {
        List<Integer> lootList = new ArrayList<>();
        ConfigurationSection lootSection = lootFile.getConfigurationSection("loot");
        //for (int i = 0; i <= 512; i++) {
        if (lootSection != null) {
            for (String key : lootSection.getKeys(false)) {
                if ((lootFile.getString("loot." + key) != null) &&
                        ((lootFile.getList("loot." + key + ".mobs") == null) ||
                                (lootFile.getStringList("loot." + key + ".mobs").contains(mob))) &&
                        (lootFile.getString("loot." + key + ".chancePercentage") == null ||
                                rand(1, 100) <= lootFile.getInt("loot." + key + ".chancePercentage"))) {
                    int lootId = Integer.parseInt(key);
                    if (mobPowerLevelFine(lootId, powers)) {
                        lootList.add(lootId);
                    }
                }
            }
        }

        try {
            if (getConfig().getBoolean("debug"))
                getLogger().log(Level.INFO, "Loot List " + lootList);
            if (!lootList.isEmpty()) {
                return getLoot(player, lootList.get(rand(1, lootList.size()) - 1));
            } else
                return null;
        } catch (Exception e) {
            getLogger().warning("Error in get random loot: No valid drops found!");
        }
        return null;
    }

    private ItemStack getLoot(Player player, int loot) {
        ItemStack i = null;
        try {
            if (!lootFile.getStringList("loot." + loot + ".commands").isEmpty()) {
                List<String> commandList = lootFile.getStringList("loot." + loot + ".commands");
                for (String command : commandList) {
                    command = ChatColor.translateAlternateColorCodes('&', command);
                    command = command.replace("player", player.getName());
                    getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
            i = getItem(loot);
        } catch (Exception x) {
            getServer().getLogger().log(Level.WARNING, "No loot found with ID: " + loot);
        }
        return i;
    }

    private Material getMaterial(String s) {
        return Material.valueOf(s);
    }

    public ItemStack getItem(int loot) {
        try {
            String setItem = lootFile.getString("loot." + loot + ".item");

            String setAmountString = lootFile.getString("loot." + loot + ".amount");
            int setAmount;
            if (setAmountString != null) {
                setAmount = getIntFromString(setAmountString);
            } else
                setAmount = 1;
            ItemStack stack = new ItemStack(getMaterial(setItem), setAmount);
            //Name
            String name = null;
            if (lootFile.getString("loot." + loot + ".name") != null && lootFile.isString("loot." + loot + ".name")) {
                name = lootFile.getString("loot." + loot + ".name");
                name = prosessLootName(name, stack);
            } else if (lootFile.isList("loot." + loot + ".name")) {
                List<String> names = lootFile.getStringList("loot." + loot + ".name");
                if (!names.isEmpty()) {
                    name = names.get(rand(1, names.size()) - 1);
                    name = prosessLootName(name, stack);
                }
            }
            //Lore
            List<String> loreList = new ArrayList<>();
            for (int i = 0; i <= 32; i++) {
                if (lootFile.getString("loot." + loot + ".lore" + i) != null) {
                    String lore = lootFile.getString("loot." + loot + ".lore" + i, "");
                    lore = ChatColor.translateAlternateColorCodes('&', lore);
                    loreList.add(lore);
                }
            }
            if (!lootFile.getStringList("loot." + loot + ".lore").isEmpty()) {
                List<String> l = lootFile.getStringList("loot." + loot + ".lore");
                int min = l.size();
                if (lootFile.getString("loot." + loot + ".minLore") != null)
                    min = lootFile.getInt("loot." + loot + ".minLore");
                int max = l.size();
                if (lootFile.getString("loot." + loot + ".maxLore") != null)
                    max = lootFile.getInt("loot." + loot + ".maxLore");
                if (!l.isEmpty())
                    for (int i = 0; i < rand(min, max); i++) {
                        String lore = l.get(rand(1, l.size()) - 1);
                        l.remove(lore);
                        loreList.add(prosessLootName(lore, stack));
                    }
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                //Durability
                if (lootFile.getString("loot." + loot + ".durability") != null) {
                    String durabilityString = lootFile.getString("loot." + loot + ".durability");
                    int durability = getIntFromString(durabilityString);
                    ((Damageable) meta).setDamage(durability);
                    //stack.setDurability((short) durability);
                }
                if (name != null) {
                    meta.setDisplayName(name);
                }
                if (!loreList.isEmpty()) {
                    meta.setLore(loreList);
                }
                stack.setItemMeta(meta);
                //Colour
                if (meta instanceof LeatherArmorMeta && lootFile.getString("loot." + loot + ".colour") != null) {
                    String c = lootFile.getString("loot." + loot + ".colour", "");
                    String[] split = c.split(",");
                    Color colour = Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    dye(stack, colour);
                }
                //Book
                if (meta instanceof BookMeta) {
                    BookMeta bMeta = (BookMeta) meta;
                    String author = lootFile.getString("loot." + loot + ".author");
                    if (author != null) {
                        author = ChatColor.translateAlternateColorCodes('&', author);
                        bMeta.setAuthor(author);
                    }
                    String title = lootFile.getString("loot." + loot + ".title", "");
                    title = ChatColor.translateAlternateColorCodes('&', title);
                    bMeta.setTitle(title);
                    ConfigurationSection lootPages = lootFile.getConfigurationSection("loot." + loot + ".pages");
                    if (lootPages != null) {
                        for (String key : lootPages.getKeys(false)) {
                            Optional.ofNullable(lootPages.getString(key))
                                    .map(str -> ChatColor.translateAlternateColorCodes('&', str))
                                    .ifPresent(bMeta::addPage);
                        }
                    }
                    stack.setItemMeta(bMeta);
                }
                //Banners
                if (meta instanceof BannerMeta) {
                    BannerMeta b = (BannerMeta) meta;
                    var patterns =
                            lootFile.getList("loot." + loot + ".patterns", Collections.emptyList())
                                    .stream()
                                    .filter(obj -> obj instanceof Pattern)
                                    .map(obj -> (Pattern) obj)
                                    .collect(Collectors.toList());

                    if (!patterns.isEmpty()) {
                        b.setPatterns(patterns);
                    }
                    stack.setItemMeta(b);
                }
                //Shield
                if (meta instanceof BlockStateMeta) {
                    BlockStateMeta bmeta = (BlockStateMeta) meta;
                    var blockState = bmeta.getBlockState();

                    if (blockState instanceof Banner) {
                        Banner b = (Banner) blockState;
                        Optional.ofNullable(lootFile.getString("loot." + loot + ".colour")).map(DyeColor::valueOf).ifPresent(b::setBaseColor);
                        var patterns =
                                lootFile.getList("loot." + loot + ".patterns", Collections.emptyList())
                                        .stream()
                                        .filter(obj -> obj instanceof Pattern)
                                        .map(obj -> (Pattern) obj)
                                        .collect(Collectors.toList());
                        b.setPatterns(patterns);
                        b.update();
                        bmeta.setBlockState(b);
                        stack.setItemMeta(bmeta);
                    }
                }
                //Owner
                if (meta instanceof SkullMeta) {
                    SkullMeta sm = (SkullMeta) meta;
                    Optional.ofNullable(lootFile.getString("loot." + loot + ".owner"))
                            .map(UUID::fromString)
                            .map(Bukkit::getOfflinePlayer)
                            .ifPresent(sm::setOwningPlayer);
                    stack.setItemMeta(sm);
                }

                //Potions
                if (meta instanceof PotionMeta) {
                    PotionMeta pMeta = (PotionMeta) meta;

                    Optional.ofNullable(lootFile.getString("loot." + loot + ".potion"))
                            .map(PotionType::valueOf)
                            .map(type -> new PotionData(type, false, false))
                            .ifPresent(pMeta::setBasePotionData);
                    stack.setItemMeta(pMeta);
                }
                int enchAmount = 0;
                for (int e = 0; e <= 10; e++) {
                    if (lootFile.getString("loot." + loot + ".enchantments." + e) != null) {
                        enchAmount++;
                    }
                }
                if (enchAmount > 0) {
                    int enMin = 0;
                    int enMax = enchAmount;
                    if ((lootFile.getString("loot." + loot + ".minEnchantments") != null) && (lootFile.getString("loot." + loot + ".maxEnchantments") != null)) {
                        enMin = lootFile.getInt("loot." + loot + ".minEnchantments");
                        enMax = lootFile.getInt("loot." + loot + ".maxEnchantments");
                    }
                    int enchNeeded = rand(enMin, enMax);
                    List<LevelledEnchantment> enchList = new ArrayList<>();
                    int safety = 0;
                    int j = 0;

                    do {
                        if (lootFile.getString("loot." + loot + ".enchantments." + j) != null) {
                            int enChance = 1;
                            if (lootFile.getString("loot." + loot + ".enchantments." + j + ".chance") != null) {
                                enChance = lootFile.getInt("loot." + loot + ".enchantments." + j + ".chance");
                            }
                            if (RANDOM.nextInt(enChance) == 0) {
                                String enchantment = lootFile.getString("loot." + loot + ".enchantments." + j + ".enchantment").toLowerCase();
                                String levelString = lootFile.getString("loot." + loot + ".enchantments." + j + ".level");
                                int level = getIntFromString(levelString);
                                if (Enchantment.getByKey(NamespacedKey.minecraft(enchantment)) != null) {
                                    if (level < 1) {
                                        level = 1;
                                    }
                                    LevelledEnchantment le = new LevelledEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment)), level);

                                    boolean con = false;
                                    for (LevelledEnchantment testE : enchList) {
                                        if (testE.getEnchantment.equals(le.getEnchantment)) {
                                            con = true;
                                            break;
                                        }
                                    }
                                    if (!con) {
                                        enchList.add(le);
                                    }
                                } else {
                                    getLogger().warning("Error: No valid drops found!");
                                    getLogger().warning("Error: " + enchantment + " is not a valid enchantment!");
                                    return null;
                                }
                            }
                        }
                        j++;
                        if (j > enchAmount) {
                            j = 0;
                            safety++;
                        }
                        if (safety >= enchAmount * 100) {
                            break;
                        }
                    } while (enchList.size() != enchNeeded);
                    for (LevelledEnchantment le : enchList) {
                        if (stack.getType() == Material.ENCHANTED_BOOK) {
                            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) stack.getItemMeta();
                            enchantMeta.addStoredEnchant(le.getEnchantment, le.getLevel, true);
                            stack.setItemMeta(enchantMeta);
                        } else {
                            stack.addUnsafeEnchantment(le.getEnchantment, le.getLevel);
                        }
                    }
                }
            }
            return stack;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), true);
            e.printStackTrace();
        }
        return null;
    }

    private void setItem(ItemStack s, String path, FileConfiguration fc) {
        ItemMeta meta;
        if (s != null && (meta = s.getItemMeta()) != null) {
            fc.set(path + ".item", s.getType().toString());
            fc.set(path + ".amount", s.getAmount());
            fc.set(path + ".durability", ((Damageable) s).getDamage());
            fc.set(path + ".name", meta.getDisplayName());
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (int l = 0; l < lore.size(); l++) {
                    if (lore.get(l) != null) {
                        fc.set(path + ".lore" + l, lore.get(l));
                    }
                }
            }
            Enchantment e;
            for (Map.Entry<Enchantment, Integer> hm : s.getEnchantments().entrySet()) {
                e = hm.getKey();
                int level = hm.getValue();
                for (int ei = 0; ei < 13; ei++) {
                    if (fc.getString(path + ".enchantments." + ei) == null) {
                        fc.set(path + ".enchantments." + ei + ".enchantment", e.getKey());
                        fc.set(path + ".enchantments." + ei + ".level", level);
                        break;
                    }
                }
            }
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta em = (EnchantmentStorageMeta) meta;
                for (Map.Entry<Enchantment, Integer> hm : em.getStoredEnchants().entrySet()) {
                    e = hm.getKey();
                    int level = hm.getValue();
                    for (int ei = 0; ei < 13; ei++) {
                        if (fc.getString(path + ".enchantments." + ei) == null) {
                            fc.set(path + ".enchantments." + ei + ".enchantment", e.toString());
                            fc.set(path + ".enchantments." + ei + ".level", level);
                            break;
                        }
                    }
                }
            }
            if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;
                if (bookMeta.getAuthor() != null) {
                    fc.set(path + ".author", bookMeta.getAuthor());
                }
                if (bookMeta.getTitle() != null) {
                    fc.set(path + ".title", bookMeta.getTitle());
                }
                int i = 0;
                for (String p : bookMeta.getPages()) {
                    fc.set(path + ".pages." + i, p);
                    i++;
                }
            }
            //Banner
            if (meta instanceof BannerMeta) {
                BannerMeta b = (BannerMeta) meta;
                var patList = b.getPatterns();
                if (!patList.isEmpty()) {
                    fc.set(path + ".patterns", patList);
                }
            }
            //Shield
            if (meta instanceof BlockStateMeta) {
                BlockStateMeta bmeta = (BlockStateMeta) meta;
                Banner b = (Banner) bmeta.getBlockState();

                fc.set(path + ".colour", b.getBaseColor().toString());
                var patList = b.getPatterns();
                if (!patList.isEmpty()) {
                    fc.set(path + ".patterns", patList);
                }
            }
            //Potions
            if (meta instanceof PotionMeta) {
                PotionMeta pMeta = (PotionMeta) meta;
                PotionData pd = pMeta.getBasePotionData();
                Optional.ofNullable(pd.getType().getEffectType())
                        .ifPresent(type -> fc.set(path + ".potion", type.getName()));
            }
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta l = (LeatherArmorMeta) meta;
                Color c = l.getColor();
                String color = c.getRed() + "," + c.getGreen() + "," + c.getBlue();
                fc.set(path + ".colour", color);
            }
            if (meta instanceof SkullMeta) {
                SkullMeta sm = (SkullMeta) meta;
                Optional.ofNullable(sm.getOwningPlayer())
                        .ifPresent(owner -> fc.set(path + ".owner", owner.getUniqueId().toString()));
            }
            List<String> flags = new ArrayList<>();
            for (ItemFlag f : meta.getItemFlags())
                if (f != null)
                    flags.add(f.name());
            if (!flags.isEmpty())
                fc.set(path + ".flags", flags);
        } else {
            getLogger().warning("Item is null!");
        }

        saveAsync(lootFile, lootYML);
        ForkJoinPool.commonPool().execute(this::saveConfig);
    }

    private String prosessLootName(String name, ItemStack stack) {
        name = ChatColor.translateAlternateColorCodes('&', name);
        String itemName = stack.getType().name();
        itemName = itemName.replace("_", " ");
        itemName = itemName.toLowerCase();
        name = name.replace("<itemName>", itemName);
        return name;
    }

    private int getIntFromString(String setAmountString) {
        int setAmount = 1;
        try {
            if (setAmountString.contains("-")) {
                String[] split = setAmountString.split("-");
                try {
                    int minSetAmount = Integer.parseInt(split[0]);
                    int maxSetAmount = Integer.parseInt(split[1]);
                    setAmount = RANDOM.nextInt(maxSetAmount - minSetAmount + 1) + minSetAmount;
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, null, e);
                }
            } else {
                setAmount = Integer.parseInt(setAmountString);
            }
        } catch (Exception ignored) {
        }
        return setAmount;

    }

    private String getEffect() {
        var particleTypes = getConfig().getStringList("mobParticles");

        if (particleTypes.isEmpty()) {
            return "mobSpawnerFire";
        } else {
            return particleTypes.get(RANDOM.nextInt(particleTypes.size()));
        }
    }

    private void displayEffect(Location l, String effect) {
        if (effect == null) {
            effect = getEffect();
        }

        // effect:speed:amount
        // but speed is not used
        String[] split = effect.split(":");

        effect = split[0];
        int amount = 2;

        if (split.length == 3) {
            try {
                amount = Integer.parseInt(split[2]);
            } catch (Exception ignored) {
            }
        }

        try {
            var f = Particle.FLAME;
            switch (effect) {
                case "potionBrake":
                    f = Particle.SPELL;
                    break;
                case "smoke":
                    f = Particle.SMOKE_NORMAL;
                    break;
                case "blockBrake":
                    f = Particle.BLOCK_CRACK;
                    break;
                case "hugeExplode":
                    f = Particle.EXPLOSION_HUGE;
                    break;
                case "angryVillager":
                    f = Particle.VILLAGER_ANGRY;
                    break;
                case "cloud":
                    f = Particle.CLOUD;
                    break;
                case "criticalHit":
                    f = Particle.CRIT;
                    break;
                case "mobSpell":
                    f = Particle.SPELL_MOB;
                    break;
                case "enchantmentTable":
                    f = Particle.ENCHANTMENT_TABLE;
                    break;
                case "ender":
                    f = Particle.PORTAL;
                    break;
                case "explode":
                    f = Particle.EXPLOSION_NORMAL;
                    break;
                case "greenSparkle":
                    f = Particle.VILLAGER_HAPPY;
                    break;
                case "heart":
                    f = Particle.HEART;
                    break;
                case "largeExplode":
                    f = Particle.EXPLOSION_LARGE;
                    break;
                case "splash":
                    f = Particle.WATER_SPLASH;
                    break;
                case "largeSmoke":
                    f = Particle.SMOKE_LARGE;
                    break;
                case "lavaSpark":
                    f = Particle.LAVA;
                    break;
                case "magicCriticalHit":
                    f = Particle.CRIT_MAGIC;
                    break;
                case "noteBlock":
                    f = Particle.NOTE;
                    break;
                case "tileDust":
                    f = Particle.BLOCK_DUST;
                    break;
                case "colouredDust":
                    f = Particle.REDSTONE;
                    break;
                case "witchMagic":
                    f = Particle.SPELL_WITCH;
                    break;
            }
            displayParticle(f, l, amount);
        } catch (Exception ignored) {
        }
    }

    private void showEffect() {
        //GUI Bars And Stuff
        scoreCheck();

        for (var infernalMob : infernalMobMap.values()) {
            var entity = infernalMob.entity;

            if (!entity.isValid() || entity.isDead() || !entity.getLocation().getChunk().isLoaded()) {
                continue;
            }

            Location head = entity.getLocation();
            head.setY(head.getY() + 1);

            if (getConfig().getBoolean("enableParticles")) {
                displayEffect(entity.getLocation(), infernalMob.effect);

                if (isAdult(entity)) {
                    displayEffect(head, infernalMob.effect);
                }

                if (entity.getType() == EntityType.ENDERMAN || entity.getType() == EntityType.IRON_GOLEM) {
                    head.setY(head.getY() + 1);
                    displayEffect(head, infernalMob.effect);
                }
            }

            //Ability's
            if (entity.isDead() || !(entity instanceof LivingEntity)) {
                continue;
            }

            var mob = (LivingEntity) entity;

            for (var ability : infernalMob.abilityList) {
                int randomNum = RANDOM.nextInt(9);

                switch (ability) {
                    case "armoured":
                        if (entity instanceof Skeleton || entity instanceof Zombie) {
                            break;
                        }
                    case "cloaked":
                    case "sprint":
                    case "molten":
                        Optional.ofNullable(EFFECT_MAP.get(ability)).ifPresent(mob::addPotionEffect);
                        break;
                    case "tosser":
                        if (randomNum < 5) {
                            double radius = 6D;
                            for (Player player : entity.getWorld().getPlayers()) {
                                if (player.getLocation().distance(entity.getLocation()) <= radius) {
                                    if (!player.isSneaking() && player.getGameMode() != GameMode.CREATIVE) {
                                        player.setVelocity(entity.getLocation().toVector().subtract(player.getLocation().toVector()));
                                    }
                                }
                            }
                        }
                        break;
                    case "gravity":
                        if (randomNum < 8) {
                            break;
                        }

                        for (Player player : entity.getWorld().getPlayers()) {
                            if (player.getLocation().distance(entity.getLocation()) <= 10) {
                                Location feetBlock = player.getLocation();
                                feetBlock.setY(feetBlock.getY() - 2);
                                var block = player.getWorld().getBlockAt(feetBlock);
                                if (block.getType() != Material.AIR && player.getGameMode() != GameMode.CREATIVE) {
                                    levitate(player, getConfig().getInt("gravityLevitateLength", 6));
                                }
                            }
                        }
                        break;
                    case "ghastly":
                    case "necromancer":
                        if (randomNum != 5) {
                            break;
                        }

                        for (Player player : entity.getWorld().getPlayers()) {
                            if (player.getGameMode() != GameMode.SURVIVAL || player.getGameMode() != GameMode.ADVENTURE) {
                                continue;
                            }

                            if (player.getLocation().distance(entity.getLocation()) <= 20) {
                                Fireball fb;
                                if (ability.equals("ghastly")) {
                                    fb = mob.launchProjectile(Fireball.class);
                                    player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                                } else {
                                    fb = mob.launchProjectile(WitherSkull.class);
                                }

                                moveToward(fb, player.getLocation(), 0.6);
                            }
                        }
                        break;
                }
            }
        }

        serverTime++;
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::showEffect, 20);
    }

    public boolean isAdult(Entity mob) {
        if (mob instanceof Ageable) {
            return ((Ageable) mob).isAdult();
        } else {
            return false;
        }
    }

    public void moveToward(final Entity e, final Location to, final double speed) {
        if (e.isDead()) {
            return;
        }
        Vector direction = to.toVector().subtract(e.getLocation().toVector()).normalize();
        e.setVelocity(direction.multiply(speed));
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                moveToward(e, to, speed);
            } catch (Exception ignored) {
            }
        }, 1L);
    }

    public void applyEffect() {
        var effectWorlds = getConfig().getStringList("effectworlds");
        var enabledAll = effectWorlds.contains("<all>");
        var enabledCharmSlots = getConfig().getIntegerList("enabledCharmSlots");

        var potionEffects = lootFile.getConfigurationSection("potionEffects");

        if (potionEffects == null) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, this::applyEffect, (10 * 20));
            return;
        }

        var idRequireItemMap = new HashMap<String, List<ItemStack>>();

        for (var id : potionEffects.getKeys(false)) {
            if (potionEffects.getString(id + ".attackEffect") != null ||
                    potionEffects.getString(id + ".attackHelpEffect") != null) {
                continue;
            }

            var requires =
                    potionEffects.getIntegerList(id + ".requiredItems")
                            .stream()
                            .map(this::getItem)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toUnmodifiableList());

            idRequireItemMap.put(id, requires);
        }

        //Check Players
        for (Player p : getServer().getOnlinePlayers()) {
            World world = p.getWorld();

            if (!enabledAll && !effectWorlds.contains(world.getName())) {
                continue;
            }

            var itemsInCharmSlots =
                    enabledCharmSlots.stream()
                            .map(index -> p.getInventory().getItem(index))
                            .filter(Objects::nonNull)
                            .filter(item -> item.getItemMeta() != null)
                            .collect(Collectors.toUnmodifiableSet());

            var armors =
                    Arrays.stream(p.getInventory().getArmorContents())
                            .filter(Objects::nonNull)
                            .filter(item -> item.getItemMeta() != null)
                            .collect(Collectors.toUnmodifiableSet());

            for (var id : idRequireItemMap.keySet()) {
                var needItems = idRequireItemMap.get(id);
                int count = 0;
                int require = needItems.size();

                for (int i = 0; i < require && count < require; i++) {
                    var need = needItems.get(i);

                    for (var armor : armors) {
                        if (armor.getType() == need.getType() &&
                                (need.getItemMeta() == null || isSameDisplayName(armor, need))) {
                            count++;

                            if (count == require) {
                                break;
                            }
                        }
                    }

                    if (require <= count) {
                        break;
                    }

                    for (var item : itemsInCharmSlots) {
                        if (item.getType() == need.getType() && !isArmor(item) &&
                                (need.getItemMeta() == null || isSameDisplayName(item, need))) {
                            count++;

                            if (count == require) {
                                break;
                            }
                        }
                    }
                }

                if (require <= count) {
                    applyEffects(p, Integer.parseInt(id));
                }
            }
        }

        getServer().getScheduler().scheduleSyncDelayedTask(this, this::applyEffect, (10 * 20));
    }

    private boolean isArmor(ItemStack s) {
        String t = s.getType().toString();
        return t.contains("HELMET") || t.contains("CHESTPLATE") || t.contains("LEGGINGS") || t.contains("BOOTS");
    }

    private boolean isSameDisplayName(ItemStack item, ItemStack other) {
        var itemMeta = item.getItemMeta();
        var otherMeta = other.getItemMeta();

        if (itemMeta == null || otherMeta == null) {
            return false;
        }

        return itemMeta.getDisplayName().equals(otherMeta.getDisplayName());
    }

    private void applyEffects(LivingEntity e, int effectID) {
        int level = lootFile.getInt("potionEffects." + effectID + ".level");

        var potionEffectType = PotionEffectType.getByName(lootFile.getString("potionEffects." + effectID + ".potion", ""));

        if (potionEffectType != null) {
            var duration =
                    potionEffectType == PotionEffectType.HARM || potionEffectType == PotionEffectType.HEAL ? 1 : 400;
            e.addPotionEffect(new PotionEffect(potionEffectType, duration, level - 1));
        }

        Optional.ofNullable(lootFile.getString("potionEffects." + effectID + ".particleEffect"))
                .ifPresent(effect -> showEffectParticles(e, effect, 15));
    }

    private void showEffectParticles(final Entity p, final String e, int time) {
        displayEffect(p.getLocation(), e);
        final int nt = time - 1;
        if (time > 0) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> showEffectParticles(p, e, nt), 20L);
        }
    }

    private void levitate(final Entity e, final int time) {
        if ((e instanceof LivingEntity)) {
            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, time * 20, 0));
        }
    }

    void doEffect(Player player, final Entity mob, boolean isPlayerVictim) {
        //Do Player Loot Effects
        if (!isPlayerVictim) {
            //Get Player Item In Hand
            ItemStack itemUsed = player.getInventory().getItemInMainHand();
            //Get Player Items
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack in = player.getInventory().getItem(i);
                if (in != null)
                    items.add(in);
            }
            for (ItemStack ar : player.getInventory().getArmorContents())
                if (ar != null)
                    items.add(ar);
            for (int i = 0; i < 256; i++) {
                if (lootFile.getString("potionEffects." + i) != null) {
                    if (lootFile.getString("potionEffects." + i + ".attackEffect") != null) {
                        boolean effectsPlayer = !lootFile.getString("potionEffects." + i + ".attackEffect", "target").equals("target");
                        for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            ItemStack neededItem = getItem(neededItemIndex);
                            try {
                                if ((neededItem.getItemMeta() == null) || (itemUsed.getItemMeta().getDisplayName().equals(neededItem.getItemMeta().getDisplayName()))) {
                                    if (itemUsed.getType() == neededItem.getType()) {
                                        //if ((neededItem.getType().getMaxDurability() > 0) || (itemUsed.getDurability() == (neededItem.getDurability()))) {
                                        //Player Using Item
                                        if (effectsPlayer) {
                                            applyEffects(player, i);
                                        } else {
                                            if (mob instanceof LivingEntity)
                                                applyEffects((LivingEntity) mob, i);
                                        }
                                        //}
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    } else if (lootFile.getString("potionEffects." + i + ".attackHelpEffect") != null) {
                        boolean effectsPlayer = !lootFile.getString("potionEffects." + i + ".attackHelpEffect", "target").equals("target");
                        List<ItemStack> itemsPlayerHas = new ArrayList<>();
                        for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            ItemStack neededItem = getItem(neededItemIndex);
                            for (ItemStack check : items) {
                                try {
                                    if ((neededItem.getItemMeta() == null) || (check.getItemMeta().getDisplayName().equals(neededItem.getItemMeta().getDisplayName()))) {
                                        if (check.getType() == neededItem.getType()) {
                                            //if ((neededItem.getType().getMaxDurability() > 0) || (check.getDurability() == (neededItem.getDurability()))) {
                                            if (!itemsPlayerHas.contains(neededItem)) {
                                                itemsPlayerHas.add(neededItem);
                                            }
                                            //}
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        if (itemsPlayerHas.size() >= lootFile.getIntegerList("potionEffects." + i + ".requiredItems").size()) {
                            //Player Using Item
                            if (effectsPlayer) {
                                applyEffects(player, i);
                            } else {
                                if (mob instanceof LivingEntity)
                                    applyEffects((LivingEntity) mob, i);
                            }
                        }
                    }
                }
            }
        }
        //Do InfernalMob Effects
        try {
            UUID id = mob.getUniqueId();
            if (infernalMobMap.containsKey(id)) {
                List<String> abilityList = findMobAbilities(id);
                if ((!player.isDead()) && (!mob.isDead())) {
                    for (String ability : abilityList)
                        doMagic(player, mob, isPlayerVictim, ability, id);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void doMagic(Entity vic, Entity atc, boolean isPlayerVictim, String ability, UUID id) {
        int min = 1;
        int max = 10;
        int randomNum = RANDOM.nextInt(max - min) + min;
        if ((atc instanceof Player)) {
            randomNum = 1;
        }
        try {
            if ((atc instanceof Player)) {
                switch (ability) {
                    case "tosser":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (((Player) vic).getGameMode() != GameMode.CREATIVE))) {
                            vic.setVelocity(atc.getLocation().toVector().subtract(vic.getLocation().toVector()));
                        }
                        break;
                    case "gravity":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (((Player) vic).getGameMode() != GameMode.CREATIVE))) {
                            Location feetBlock = vic.getLocation();
                            feetBlock.setY(feetBlock.getY() - 2.0D);
                            Block block = vic.getWorld().getBlockAt(feetBlock);
                            if (block.getType() != Material.AIR) {
                                int amount = 6;
                                if (getConfig().getString("gravityLevitateLength") != null) {
                                    amount = getConfig().getInt("gravityLevitateLength");
                                }
                                levitate(vic, amount);
                            }
                        }
                        break;
                    case "ghastly":
                    case "necromancer":
                        if ((!vic.isDead()) && ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (((Player) vic).getGameMode() != GameMode.CREATIVE)))) {
                            Fireball fb;
                            if (ability.equals("ghastly")) {
                                fb = ((LivingEntity) atc).launchProjectile(Fireball.class);
                            } else {
                                fb = ((LivingEntity) atc).launchProjectile(WitherSkull.class);
                            }
                            moveToward(fb, vic.getLocation(), 0.6D);
                        }
                        break;
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if ((ability.equals("poisonous")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            } else if ((ability.equals("morph")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                try {
                    Entity newEnt;
                    int mc = RANDOM.nextInt(25);
                    if (mc != 20) {
                        return;
                    }
                    Location l = atc.getLocation().clone();

                    double dis = 46.0D;
                    for (Entity e : atc.getNearbyEntities(dis, dis, dis))
                        if (e instanceof Player)
                            GUI.fixBar(((Player) e));
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0D, l.getZ()));
                    atc.remove();
                    getLogger().log(Level.INFO, "Entity remove due to Morph");
                    List<String> mList = getConfig().getStringList("enabledmobs");
                    int index = RANDOM.nextInt(mList.size());
                    String mobName = mList.get(index);

                    newEnt =
                            Optional.ofNullable(getEntityTypeFromName(mobName))
                                    .map(type -> vic.getWorld().spawnEntity(l, type))
                                    .orElse(null);

                    if (newEnt == null) {
                        getLogger().warning("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    List<String> abilities = infernalMobMap.get(id).abilityList;

                    var newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, abilities, 2, getEffect());

                    if (abilities.contains("flying")) {
                        makeFly(newEnt);
                    }

                    addNewInfernalMobToMap(newMob);
                    gui.setName(newEnt);

                    giveMobGear(newMob, true);

                    addHealth(newMob, abilities);
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "Morph Error", ex);
                }
            }
            if ((ability.equals("molten")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                int amount;
                if (getConfig().getString("moltenBurnLength") != null) {
                    amount = getConfig().getInt("moltenBurnLength");
                } else {
                    amount = 5;
                }
                vic.setFireTicks(amount * 20);
            } else if ((ability.equals("blinding")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            } else if ((ability.equals("confusing")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 2));
            } else if ((ability.equals("withering")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1));
            } else if ((ability.equals("thief")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                if ((vic instanceof Player)) {
                    if (((Player) vic).getInventory().getItemInMainHand().getType() != Material.AIR && randomNum <= 1) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInMainHand());
                        int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, null);
                    }
                } else if (vic instanceof Zombie || vic instanceof Skeleton) {
                    Optional.ofNullable(((LivingEntity) vic).getEquipment())
                            .ifPresent(equipment -> {
                                vic.getWorld().dropItemNaturally(atc.getLocation(), equipment.getItemInMainHand());
                                equipment.setItemInMainHand(null);
                            });
                }
            } else if ((ability.equals("quicksand")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 1));
            } else if ((ability.equals("bullwark")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 2));
            } else if ((ability.equals("rust")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ItemStack damItem = ((Player) vic).getInventory().getItemInMainHand();
                if (randomNum <= 3 && damItem.getMaxStackSize() == 1) {
                    int cDur = ((Damageable) damItem.getItemMeta()).getDamage();
                    ((Damageable) damItem.getItemMeta()).setDamage(cDur + 20);
                }
            } else if ((ability.equals("sapper")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1));
            } else if ((!ability.equals("1up")) || (!isLegitVictim(atc, isPlayerVictim, ability))) {
                Location needAir2;
                if ((ability.equals("ender")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                    Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        int randomNum2 = RANDOM.nextInt(4);
                        if (randomNum2 == 0) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0D);
                        } else if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0D);
                        } else if (randomNum2 == 2) {
                            targetLocation.setX(targetLocation.getX() + 8.0D);
                        } else {
                            targetLocation.setX(targetLocation.getX() - 10.0D);
                        }
                        needAir2 = targetLocation;
                        needAir2.setY(needAir2.getY() + 1.0D);
                        targetLocation.setY(targetLocation.getY() + 2.0D);
                        if (((targetLocation.getBlock().getType() == Material.AIR) || (targetLocation.getBlock().getType() == Material.TORCH)) &&
                                ((needAir2.getBlock().getType() == Material.AIR) || (needAir2.getBlock().getType() == Material.TORCH)) && (
                                (targetLocation.getBlock().getType() == Material.AIR) || (targetLocation.getBlock().getType() == Material.TORCH))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if ((ability.equals("lifesteal")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1));
                } else if ((!ability.equals("cloaked")) || (!isLegitVictim(atc, isPlayerVictim, ability))) {
                    if ((ability.equals("storm")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                        if (randomNum <= 2 && !atc.isDead()) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if ((!ability.equals("sprint")) || (!isLegitVictim(atc, isPlayerVictim, ability))) {
                        if ((ability.equals("webber")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            if ((randomNum >= 8) || (randomNum == 1)) {
                                Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.COBWEB);
                                setAir(feet, 60);

                                if (RANDOM.nextInt(9) == 5 && (
                                        (atc.getType() == EntityType.SPIDER) || (atc.getType() == EntityType.CAVE_SPIDER))) {
                                    Location l = atc.getLocation();
                                    Block b = l.getBlock();
                                    List<Block> blocks = getSphere(b);
                                    for (Block bl : blocks) {
                                        if (bl.getType() == Material.AIR) {
                                            bl.setType(Material.COBWEB);
                                            setAir(bl.getLocation(), 30);
                                        }
                                    }
                                }
                            }
                        } else if ((ability.equals("vengeance")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            if ((randomNum >= 5) || (randomNum == 1)) {
                                int amount;
                                if (getConfig().getString("vengeanceDamage") != null) {
                                    amount = getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount = 6;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("weakness")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
                        } else if ((ability.equals("berserk")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            if ((randomNum >= 5) && (!atc.isDead())) {
                                double health = ((org.bukkit.entity.Damageable) atc).getHealth();
                                ((org.bukkit.entity.Damageable) atc).setHealth(health - 1.0D);
                                int amount;
                                if (getConfig().getString("berserkDamage") != null) {
                                    amount = getConfig().getInt("berserkDamage");
                                } else {
                                    amount = 3;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("potions")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            ItemStack iStack = new ItemStack(Material.POTION);
                            var potion = (PotionMeta) iStack.getItemMeta();
                            if (potion != null) {
                                switch (randomNum) {
                                    case 5:
                                        potion.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 2), true);
                                    case 6:
                                        potion.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);
                                    case 7:
                                        potion.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, (20 * 15), 2), true);
                                    case 8:
                                        potion.addCustomEffect(new PotionEffect(PotionEffectType.POISON, (20 * 5), 2), true);
                                    case 9:
                                        potion.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, (20 * 10), 2), true);
                                }
                                iStack.setItemMeta(potion);
                            }

                            Location sploc = atc.getLocation();
                            sploc.setY(sploc.getY() + 3.0D);
                            ThrownPotion thrownPotion = (ThrownPotion) vic.getWorld().spawnEntity(sploc, EntityType.SPLASH_POTION);
                            thrownPotion.setItem(iStack);
                            Vector direction = atc.getLocation().getDirection();
                            direction.normalize();
                            direction.add(new Vector(0.0D, 0.2D, 0.0D));

                            double dist = atc.getLocation().distance(vic.getLocation());

                            dist /= 15.0D;
                            direction.multiply(dist);
                            thrownPotion.setVelocity(direction);
//                }
                        } else if ((ability.equals("mama")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            if (randomNum == 1) {
                                int amount;
                                if (getConfig().getString("mamaSpawnAmount") != null) {
                                    amount = getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount = 3;
                                }

                                if (atc instanceof Ageable) {
                                    for (int i = 0; i < amount; i++) {
                                        Ageable minion = (Ageable) atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                        minion.setBaby();
                                    }
                                } else {
                                    for (int i = 0; i < amount; i++) {
                                        atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if ((ability.equals("archer")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            if ((randomNum > 7) || (randomNum == 1)) {
                                List<Arrow> arrowList = new ArrayList<>();
                                Location loc1 = vic.getLocation();
                                Location loc2 = atc.getLocation();
                                if (isAdult(atc)) {
                                    loc2.setY(loc2.getY() + 1.0D);
                                }
                                Arrow a = ((LivingEntity) atc).launchProjectile(Arrow.class);
                                int arrowSpeed = 1;
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() + 0.5D);
                                loc2.setZ(loc2.getBlockZ() + 0.5D);
                                Arrow a2 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a2.setShooter((LivingEntity) atc);
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() - 1);
                                loc2.setZ(loc2.getBlockZ() - 1);
                                Arrow a3 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a3.setShooter((LivingEntity) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586D;
                                    Entity minEntity = null;
                                    for (Entity entity : atc.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                                        if ((((LivingEntity) atc).hasLineOfSight(entity)) && ((entity instanceof LivingEntity)) && (!entity.isDead())) {
                                            Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                                            double angle = ar.getVelocity().angle(toTarget);
                                            if (angle < minAngle) {
                                                minAngle = angle;
                                                minEntity = entity;
                                            }
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, this);
                                    }
                                }
                            }
                        } else if ((ability.equals("firework")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                            int red = getConfig().getInt("fireworkColour.red");
                            int green = getConfig().getInt("fireworkColour.green");
                            int blue = getConfig().getInt("fireworkColour.blue");
                            ItemStack tmpCol = new ItemStack(Material.LEATHER_HELMET, 1);
                            Optional.ofNullable((LeatherArmorMeta) tmpCol.getItemMeta())
                                    .ifPresent(meta -> {
                                        var color = Color.fromRGB(red, green, blue);
                                        meta.setColor(Color.fromRGB(red, green, blue));
                                        launchFirework(atc.getLocation(), color);
                                    });
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static List<Block> getSphere(Block block1) {
        List<Block> blocks = new LinkedList<>();
        double xi = block1.getLocation().getX() + 0.5D;
        double yi = block1.getLocation().getY() + 0.5D;
        double zi = block1.getLocation().getZ() + 0.5D;
        for (int v1 = 0; v1 <= 90; v1++) {
            double y = Math.sin(0.017453292519943295D * v1) * 4;
            double r = Math.cos(0.017453292519943295D * v1) * 4;
            if (v1 == 90) {
                r = 0.0D;
            }
            for (int v2 = 0; v2 <= 90; v2++) {
                double x = Math.sin(0.017453292519943295D * v2) * r;
                double z = Math.cos(0.017453292519943295D * v2) * r;
                if (v2 == 90) {
                    z = 0.0D;
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    private void launchFirework(Location l, Color c) {
        Firework fw = l.getWorld().spawn(l, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(1));
        detonate(fw);
    }

    private void detonate(final Firework fw) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                fw.detonate();
            } catch (Exception ignored) {
            }
        }, 2L);
    }

    private boolean isLegitVictim(Entity e, boolean isPlayerVictim, String ability) {
        if ((e instanceof Player)) {
            return true;
        }
        if (getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        List<String> attackAbilityList = new ArrayList<>();
        attackAbilityList.add("poisonous");
        attackAbilityList.add("blinding");
        attackAbilityList.add("withering");
        attackAbilityList.add("thief");
        attackAbilityList.add("sapper");
        attackAbilityList.add("lifesteal");
        attackAbilityList.add("storm");
        attackAbilityList.add("webber");
        attackAbilityList.add("weakness");
        attackAbilityList.add("berserk");
        attackAbilityList.add("potions");
        attackAbilityList.add("archer");
        attackAbilityList.add("confusing");
        if ((isPlayerVictim) && (attackAbilityList.contains(ability))) {
            return true;
        }
        List<String> defendAbilityList = new ArrayList<>();
        defendAbilityList.add("thief");
        defendAbilityList.add("storm");
        defendAbilityList.add("webber");
        defendAbilityList.add("weakness");
        defendAbilityList.add("potions");
        defendAbilityList.add("archer");
        defendAbilityList.add("quicksand");
        defendAbilityList.add("bullwark");
        defendAbilityList.add("rust");
        defendAbilityList.add("ender");
        defendAbilityList.add("vengeance");
        defendAbilityList.add("mama");
        defendAbilityList.add("firework");
        defendAbilityList.add("morph");
        return (!isPlayerVictim) && (defendAbilityList.contains(ability));
    }

    private void setAir(final Location block, int time) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (block.getBlock().getType() == Material.COBWEB) {
                block.getBlock().setType(Material.AIR);
            }
        }, time * 20L);
    }

    private List<String> getAbilitiesAmount(Entity e) {
        int power;
        if (getConfig().getBoolean("powerByDistance")) {
            Location l = e.getWorld().getSpawnLocation();
            int m = (int) l.distance(e.getLocation()) / getConfig().getInt("addDistance");
            if (m < 1) {
                m = 1;
            }
            int add = getConfig().getInt("powerToAdd");
            power = m * add;
        } else {
            int min = getConfig().getInt("minpowers");
            int max = getConfig().getInt("maxpowers");
            power = rand(min, max);
        }
        return getAbilities(power);
    }

    private List<String> getAbilities(int amount) {
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        List<String> abilityList = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            int max = allAbilitiesList.size();
            int randomNum = RANDOM.nextInt(max);
            String ab = allAbilitiesList.get(randomNum);
            if (getConfig().getString(ab) != null) {
                if ((getConfig().getString(ab, "always").equals("always")) || (getConfig().getBoolean(ab))) {
                    abilityList.add(ab);
                    allAbilitiesList.remove(randomNum);
                } else {
                    allAbilitiesList.remove(randomNum);
                    i = i - 1;
                }
            } else
                getLogger().log(Level.WARNING, "Ability: " + ab + " is not set!");
        }
        return abilityList;
    }

    public List<String> findMobAbilities(UUID id) {
        return Optional.ofNullable(infernalMobMap.get(id)).map(i -> i.abilityList).orElse(Collections.emptyList());
    }

    private Entity getTarget(final Player player) {

        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return entity;
                            }
            }
        }
        return null;
    }

    private void makeFly(Entity ent) {
        Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
        bat.setVelocity(new Vector(0, 1, 0));
        bat.addPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1));
    }

    private void giveMobGear(InfernalMob infernalMob, boolean naturalSpawn) {
        if (!(infernalMob.entity instanceof LivingEntity)) {
            return;
        }

        var mob = (LivingEntity) infernalMob.entity;

        var mobAbilityList = infernalMob.abilityList;

        var equipments = mob.getEquipment();

        if (equipments != null) {
            if (mobAbilityList.contains("armoured")) {
                var helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
                var chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
                var leggings = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
                var boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
                var sword = new ItemStack(Material.DIAMOND_SWORD, 1);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);

                if (mob.getType() == EntityType.WITHER_SKELETON) {
                    equipments.setHelmetDropChance(0.0F);
                    equipments.setChestplateDropChance(0.0F);
                    equipments.setLeggingsDropChance(0.0F);
                    equipments.setBootsDropChance(0.0F);
                    equipments.setItemInMainHandDropChance(0.0F);
                    equipments.setHelmet(helmet);
                    equipments.setChestplate(chestplate);
                    equipments.setLeggings(leggings);
                    equipments.setBoots(boots);
                    equipments.setItemInMainHand(sword);
                }

                if (mob.getType() == EntityType.SKELETON || mob instanceof Zombie) {
                    equipments.setHelmetDropChance(0.0F);
                    equipments.setChestplateDropChance(0.0F);
                    equipments.setHelmet(helmet);
                    equipments.setChestplate(chestplate);

                    if (!mobAbilityList.contains("cloaked")) {
                        equipments.setLeggingsDropChance(0.0F);
                        equipments.setBootsDropChance(0.0F);
                        equipments.setLeggings(leggings);
                        equipments.setBoots(boots);
                    }

                    equipments.setItemInMainHandDropChance(0.0F);
                    equipments.setItemInMainHand(sword);
                }

                mob.setCanPickupItems(false);
            } else {
                if (mob.getType() == EntityType.SKELETON) {
                    ItemStack bow = new ItemStack(Material.BOW, 1);
                    equipments.setItemInMainHand(bow);

                    if (mobAbilityList.contains("cloaked")) {
                        ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                        equipments.setHelmet(skull);
                    }
                }

                if (mob instanceof Zombie && mobAbilityList.contains("cloaked")) {
                    ItemStack skull = new ItemStack(Material.GLASS_BOTTLE);
                    equipments.setHelmet(skull);
                }
            }
        }

        if (mobAbilityList.contains("mounted") && (!naturalSpawn || getConfig().getStringList("enabledRiders").contains(mob.getType().name()))) {
            var mounts = getConfig().getStringList("enabledMounts");
            var mount = mounts.get(RANDOM.nextInt(mounts.size()));
            var type = getEntityTypeFromName(mount);

            if (type == null || type == EntityType.ENDER_DRAGON) {
                getLogger().warning("Can't spawn mount because " + mount + " is not a valid entity!");
                return;
            }

            Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), type);

            mountList.put(liveMount, mob);
            liveMount.addPassenger(mob);

            if (liveMount instanceof Horse) {
                Horse horse = (Horse) liveMount;

                if (getConfig().getBoolean("horseMountsHaveSaddles")) {
                    horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                }

                horse.setTamed(true);

                Horse.Color color;

                switch (RANDOM.nextInt(7)) {
                    case 1:
                        color = Horse.Color.BLACK;
                        break;
                    case 2:
                        color = Horse.Color.BROWN;
                        break;
                    case 3:
                        color = Horse.Color.CHESTNUT;
                        break;
                    case 4:
                        color = Horse.Color.CREAMY;
                        break;
                    case 5:
                        color = Horse.Color.DARK_BROWN;
                        break;
                    case 6:
                        color = Horse.Color.GRAY;
                        break;
                    default:
                        color = Horse.Color.WHITE;
                        break;
                }

                horse.setColor(color);

                if (mobAbilityList.contains("armoured") && getConfig().getBoolean("armouredMountsHaveArmour")) {
                    horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1));
                }
            }

            if (liveMount instanceof Sheep) {
                Sheep sheep = (Sheep) liveMount;
                var colonIndex = mount.indexOf(':');

                if (colonIndex != -1 && colonIndex + 1 == mount.length()) {
                    try {
                        sheep.setColor(DyeColor.valueOf(mount.substring(colonIndex + 1)));
                    } catch (Exception e) {
                        getLogger().warning(mount + " is an invalid sheep!");
                    }
                }
            }
        }

    }

    void displayLavaParticle(World w, double x, double y, double z) {
        w.spawnParticle(Particle.DRIP_LAVA, x, y, z, 0, 0, 0, -1, 1);
    }

    private void displayParticle(Particle effect, Location l, int amount) {
        var world = l.getWorld();

        if (world == null) {
            return;
        }

        List<Location> ll = getParticleArea(l);
        if (ll.size() > 0) {
            for (int i = 0; i < amount; i++) {
                int index = RANDOM.nextInt(ll.size());
                world.spawnParticle(effect, ll.get(index), 1, 0, 0, 0, 0);
                ll.remove(index);
            }
        }
    }

    private List<Location> getParticleArea(Location l) {
        List<Location> ll = new ArrayList<>();
        for (double x = l.getX() - 1; x < l.getX() + 1; x += 0.2) {
            for (double y = l.getY() - 1; y < l.getY() + 1; y += 0.2) {
                for (double z = l.getZ() - 1; z < l.getZ() + 1; z += 0.2) {
                    ll.add(new Location(l.getWorld(), x, y, z));
                }
            }
        }
        return ll;
    }

    private String getRandomMob() {
        List<String> mobList = getConfig().getStringList("enabledmobs");
        if (mobList.isEmpty()) {
            return "Zombie";
        }
        String mob = mobList.get(rand(1, mobList.size()) - 1);
        if (mob != null) {
            return mob;
        }
        return "Zombie";
    }

    String generateString(int maxNames, List<String> names) {
        StringBuilder namesString = new StringBuilder();
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; i++) {
            namesString.append(names.get(i)).append(" ");
        }
        if (names.size() > maxNames) {
            namesString.append("... ");
        }
        return namesString.toString();
    }

    private void reloadLoot() {
        if (lootYML == null) {
            lootYML = new File(getDataFolder(), "loot.yml");
        }
        lootFile = YamlConfiguration.loadConfiguration(lootYML);

        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lootYML);
        lootFile.setDefaults(defConfig);
    }

    String getLocationName(Location l) {
        return (l.getX() + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
    }

    private boolean cSpawn(CommandSender sender, String mob, Location l, List<String> abList) {
        //cspawn <mob> <world> <x> <y> <z> <ability> <ability>
        var type = getEntityTypeFromName(mob);
        if (type != null) {
            Entity ent = l.getWorld().spawnEntity(l, type);

            UUID id = ent.getUniqueId();
            InfernalMob newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());

            if (abList.contains("1up")) {
                Abilities.ONE_UP.onSpawn((Mob) ent); // TODO: change LivingEntity to Mob
            }

            if (abList.contains("flying")) {
                makeFly(ent);
            }

            addNewInfernalMobToMap(newMob);
            gui.setName(ent);

            giveMobGear(newMob, false);
            addHealth(newMob, abList);
            return true;
        } else {
            sender.sendMessage("Can't spawn a " + mob + "!");
            return false;
        }
    }

    private int rand(int min, int max) {
        return min + (int) (RANDOM.nextDouble() * (1 + max - min));
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        Set<String> commands = new HashSet<>(Arrays.asList("reload", "worldInfo", "error", "getloot", "setloot",
                "giveloot", "abilities", "showAbilities", "setInfernal", "spawn", "cspawn", "pspawn", "kill",
                "killall", "purge"));
        if (sender.hasPermission("infernal_mobs.commands")) {

            List<String> newTab = new ArrayList<>();
            if (args.length == 1) {
                if (args[0].isEmpty())
                    return new ArrayList<>(commands);
                commands.forEach(tab -> {
                    if (tab.toLowerCase().startsWith(args[0].toLowerCase()))
                        newTab.add(tab);
                });
            }
            if (args[0].equalsIgnoreCase("getloot") || args[0].equalsIgnoreCase("setloot")) {
                if (args.length == 2) {
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("giveloot")) {
                if (args.length == 2) {
                    newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                }
                if (args.length == 3) {
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("setinfernal")) {
                if (args.length == 2) {
                    newTab.add("10");
                }
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("cspawn") || args[0].equalsIgnoreCase("pspawn")) {
                    if (args[1].isEmpty())
                        newTab.addAll(Arrays.stream(EntityType.values()).filter(m -> m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()));
                    else
                        Arrays.stream(EntityType.values()).filter(m -> m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("killall")) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("kill")) {
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("cspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3 && args.length < 7) {
                    newTab.add("~");
                }
                if (args.length >= 7) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args[0].equalsIgnoreCase("pspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("spawn")) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            return newTab;
        }
        return null;
    }

    public ItemStack getDiviningStaff() {
        ItemStack s = getBlazeRod(Collections.singletonList("Click to find infernal mobs."));

        Optional.ofNullable(s.getItemMeta()).ifPresent(meta -> {
            meta.addEnchant(Enchantment.CHANNELING, 1, true);
            s.setItemMeta(meta);
        });

        return s;
    }

    public void addRecipes() {
        ItemStack staff = getDiviningStaff();
        NamespacedKey key = new NamespacedKey(this, "divining_staff");
        ShapedRecipe sr = new ShapedRecipe(key, staff);
        sr.shape("ANA", "ASA", "ASA");
        sr.setIngredient('N', Material.NETHER_STAR);
        sr.setIngredient('S', Material.BLAZE_ROD);
        //sr.setIngredient('A', Material.AIR);
        Bukkit.addRecipe(sr);
    }

    private ItemStack getBlazeRod(List<String> loreList) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta m = item.getItemMeta();

        if (m != null) {
            m.setDisplayName("§6§lDivining Rod");
            m.setLore(loreList);
            item.setItemMeta(m);
        }

        return item;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("infernal_mobs.commands")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
        }

        try {
            Player player = null;
            if (!(sender instanceof Player)) {
                if (0 < args.length && !args[0].equalsIgnoreCase("cspawn") &&
                        !args[0].equalsIgnoreCase("pspawn") &&
                        !args[0].equalsIgnoreCase("giveloot") &&
                        !args[0].equalsIgnoreCase("reload") &&
                        !args[0].equalsIgnoreCase("killall") &&
                        !args[0].equalsIgnoreCase("purge")) {
                    sender.sendMessage("This command can only be run by a player!");
                    return true;
                }
            } else {
                player = (Player) sender;
            }

            if (args.length == 0) {
                throwError(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("slotTest") && player != null) {
                for (int i : getConfig().getIntegerList("enabledCharmSlots"))
                    player.getInventory().setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
            } else if ((args.length == 1) && (args[0].equalsIgnoreCase("fixloot"))) {
                var itemSection = getConfig().getConfigurationSection("items");
                var lootSection = lootFile.getConfigurationSection("loot");

                if (itemSection == null || lootSection == null) {
                    sender.sendMessage("Unable to find items section!");
                    return true;
                }

                var itemKeys = itemSection.getKeys(false);

                for (String i : lootSection.getKeys(false)) {
                    String oid = String.valueOf(lootFile.getInt("loot." + i + ".item"));
                    if (itemKeys.contains(oid)) {
                        lootFile.set("loot." + i + ".item", getConfig().getString("items." + oid));
                    } else {
                        getLogger().warning("ERROR in loot section: " + oid);
                    }
                }

                saveAsync(lootFile, lootYML);
                sender.sendMessage("§eLoot Fixed!");
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                reloadLoot();
                sender.sendMessage("§eConfig reloaded!");
            } else if (args[0].equalsIgnoreCase("purge")) {
                var notLoadedMobs =
                        mobSaveFile.getKeys(false)
                                .stream()
                                .map(str -> {
                                    try {
                                        return UUID.fromString(str);
                                    } catch (IllegalArgumentException e) {
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .filter(id -> !infernalMobMap.containsKey(id))
                                .map(UUID::toString)
                                .collect(Collectors.toUnmodifiableSet());

                int deleted = 0;
                for (var id : notLoadedMobs) {
                    mobSaveFile.set(id, null);
                    deleted++;
                }
                saveAsync(mobSaveFile, saveYML);
                sender.sendMessage("§e" + deleted + " mobs which are not loaded has been deleted!");
            } else if (args[0].equals("mobList")) {
                sender.sendMessage("§6Mob List:");
                Arrays.stream(EntityType.values())
                        .map(EntityType::getKey)
                        .map(NamespacedKey::getKey)
                        .forEach(name -> sender.sendMessage("§e" + name));
                return true;
            } else if ((args.length == 1) && (args[0].equalsIgnoreCase("error"))) {
                errorList.add(player);
                sender.sendMessage("§eClick on a mob to send an error report about it.");
            } else if ((args.length == 1) && (args[0].equalsIgnoreCase("info"))) {
                sender.sendMessage("§eMounts: " + mountList.size());
                sender.sendMessage("§eInfernals: " + infernalMobMap.size());
            } else if ((args.length == 1) && (args[0].equalsIgnoreCase("worldInfo"))) {
                List<String> enWorldList = getConfig().getStringList("mobworlds");
                World world = player.getWorld();
                String enabled = "is not";
                if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                    enabled = "is";
                }
                sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                sender.sendMessage("All the worlds that are enabled are: " + enWorldList);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                throwError(sender);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("getloot") && player != null) {
                int min = getConfig().getInt("minpowers");
                int max = getConfig().getInt("maxpowers");
                int powers = rand(min, max);
                ItemStack gottenLoot = getRandomLoot(player, getRandomMob(), powers);
                if (gottenLoot != null) {
                    player.getInventory().addItem(gottenLoot);
                }
                sender.sendMessage("§eGave you some random loot!");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("getloot") && player != null) {
                try {
                    int index = Integer.parseInt(args[1]);
                    ItemStack i = getLoot(player, index);
                    if (i != null) {
                        player.getInventory().addItem(i);
                        sender.sendMessage("§eGave you the loot at index §9" + index);
                        return true;
                    }
                } catch (Exception ignored) {
                }
                sender.sendMessage("§cUnable to get that loot!");
            } else if ((args.length == 3) && (args[0].equalsIgnoreCase("giveloot"))) {
                try {
                    Player p = getServer().getPlayer(args[1]);
                    if (p != null) {
                        int index = Integer.parseInt(args[2]);
                        ItemStack i = getLoot(p, index);
                        if (i != null) {
                            p.getInventory().addItem(i);
                            sender.sendMessage("§eGave the player the loot at index §9" + index);
                            return true;
                        }
                    } else {
                        sender.sendMessage("§cPlayer not found!!");
                        return true;
                    }
                } catch (Exception ignored) {
                }
                sender.sendMessage("§cUnable to get that loot!");
            } else if (((args.length == 2) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length == 6))) {
                var type = getEntityTypeFromName(args[1]);
                if (type != null) {
                    boolean exmsg = false;
                    World world;
                    Entity ent = null;
                    if ((args[0].equalsIgnoreCase("cspawn")) && (args[2] != null) && (args[3] != null) && (args[4] != null) && (args[5] != null)) {
                        if (getServer().getWorld(args[2]) == null) {
                            sender.sendMessage(args[2] + " dose not exist!");
                            return true;
                        }
                        world = getServer().getWorld(args[2]);
                        if (world != null) {
                            Location spoint = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                            ent = world.spawnEntity(spoint, type);
                            exmsg = true;
                        }
                    } else if (player != null) {
                        Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                        farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                        ent = player.getWorld().spawnEntity(farSpawnLoc, type);
                    }

                    if (ent == null) {
                        sender.sendMessage("Could not spawn the mob!");
                        return true;
                    }

                    List<String> abList = getAbilitiesAmount(ent);
                    UUID id = ent.getUniqueId();
                    InfernalMob newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());

                    if (abList.contains("flying")) {
                        makeFly(ent);
                    }

                    addNewInfernalMobToMap(newMob);
                    gui.setName(ent);

                    giveMobGear(newMob, false);
                    addHealth(newMob, abList);
                    if (!exmsg) {
                        sender.sendMessage("Spawned a " + args[1]);
                    } else {
                        sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5]);
                    }
                } else {
                    sender.sendMessage("Can't spawn a " + args[1] + "!");
                    return true;
                }
            } else if (((args.length >= 3) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length >= 6)) || ((args[0].equalsIgnoreCase("pspawn")) && (args.length >= 3))) {
                if (args[0].equalsIgnoreCase("spawn")) {
                    var type = getEntityTypeFromName(args[1]);
                    if (type != null) {
                        Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                        farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                        Entity ent = player.getWorld().spawnEntity(farSpawnLoc, type);
                        List<String> spesificAbList = new ArrayList<>();
                        for (int i = 0; i <= args.length - 3; i++) {
                            if (getConfig().getString(args[(i + 2)]) != null) {
                                spesificAbList.add(args[(i + 2)]);
                            } else {
                                sender.sendMessage(args[(i + 2)] + " is not a valid ability!");
                                return true;
                            }
                        }

                        UUID id = ent.getUniqueId();
                        InfernalMob newMob = new InfernalMob(ent, id, true, spesificAbList, 1, getEffect());

                        if (spesificAbList.contains("flying")) {
                            makeFly(ent);
                        }
                        addNewInfernalMobToMap(newMob);
                        gui.setName(ent);
                        giveMobGear(newMob, false);

                        addHealth(newMob, spesificAbList);

                        sender.sendMessage("Spawned a " + args[1] + " with the abilities:");
                        sender.sendMessage(spesificAbList.toString());
                    } else {
                        sender.sendMessage("Can't spawn a " + args[1] + "!");
                    }
                } else if (args[0].equalsIgnoreCase("cspawn")) {
                    //cspawn <mob> <world> <x> <y> <z> <ability> <ability>
                    if (getServer().getWorld(args[2]) == null) {
                        sender.sendMessage(args[2] + " dose not exist!");
                        return true;
                    }
                    World world = getServer().getWorld(args[2]);
                    Location spoint = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    List<String> abList = Arrays.asList(args).subList(6, args.length);
                    if (cSpawn(sender, args[1], spoint, abList)) {
                        sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5] + " with the abilities:");
                        sender.sendMessage(abList.toString());
                    }
                } else {
                    //pspawn <mob> <player> <ability> <ability>
                    Player p = getServer().getPlayer(args[2]);
                    if (p == null) {
                        sender.sendMessage(args[2] + " is not online!");
                        return true;
                    }
                    List<String> abList = Arrays.asList(args).subList(3, args.length);
                    if (cSpawn(sender, args[1], p.getLocation(), abList)) {
                        sender.sendMessage("Spawned a " + args[1] + " at " + p.getName() + " with the abilities:");
                        sender.sendMessage(abList.toString());
                    }
                }
            } else if ((args.length == 1) && (args[0].equalsIgnoreCase("abilities"))) {
                sender.sendMessage("--Infernal Mobs Abilities--");
                sender.sendMessage("mama, molten, weakness, vengeance, webber, storm, sprint, lifesteal, ghastly, ender, cloaked, berserk, 1up, sapper, rust, bullwark, quicksand, thief, tosser, withering, blinding, armoured, poisonous, potions, explode, gravity, archer, necromancer, firework, flying, mounted, morph, ghost, confusing");
            } else {
                List<String> oldMobAbilityList;
                if ((args.length == 1) && (args[0].equalsIgnoreCase("showAbilities"))) {
                    var target = getTarget(player);
                    if (target != null) {
                        UUID mobId = target.getUniqueId();
                        if (infernalMobMap.containsKey(mobId)) {
                            oldMobAbilityList = findMobAbilities(mobId);
                            if (!target.isDead()) {
                                sender.sendMessage("--Targeted InfernalMob's Abilities--");
                                sender.sendMessage(oldMobAbilityList.toString());
                            }
                        } else {
                            sender.sendMessage("§cThis " + target.getType().getKey().getKey() + " §cis not an infernalmob!");
                        }
                    } else {
                        sender.sendMessage("§cUnable to find mob!");
                    }
                } else if ((args[0].equalsIgnoreCase("setInfernal")) && (args.length == 2)) {
                    if (player.getTargetBlock(null, 25).getType() == Material.SPAWNER) {
                        int delay = Integer.parseInt(args[1]);

                        String name = getLocationName(player.getTargetBlock(null, 25).getLocation());

                        mobSaveFile.set("infernalSpawners." + name, delay);
                        saveAsync(mobSaveFile, saveYML);
                        sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                    } else {
                        sender.sendMessage("§cYou must be looking a spawner to make it infernal!");
                    }
                } else if ((args[0].equalsIgnoreCase("kill")) && (args.length == 2)) {
                    int size = Integer.parseInt(args[1]);
                    player.getNearbyEntities(size, size, size)
                            .stream()
                            .map(Entity::getUniqueId)
                            .map(infernalMobMap::get)
                            .filter(Objects::nonNull)
                            .forEach(infernalMob -> {
                                infernalMob.entity.remove();
                                removeMob(infernalMob);
                            });
                    sender.sendMessage("§eKilled all infernal mobs near you!");
                } else if ((args[0].equalsIgnoreCase("killall")) && (args.length == 1 || args.length == 2) && player != null) {
                    World w;

                    if (args.length == 1) {
                        w = ((Player) sender).getWorld();
                    } else {
                        w = getServer().getWorld(args[1]);
                    }

                    if (w == null) {
                        sender.sendMessage("§cWorld not found!");
                        return true;
                    }

                    for (var infernalMob : List.copyOf(infernalMobMap.values())) {
                        if (infernalMob.entity.getWorld().equals(w)) {
                            removeMob(infernalMob, false);
                        }
                    }

                    saveAsync(mobSaveFile, saveYML);
                    sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                } else if (args[0].equalsIgnoreCase("mobs")) {
                    sender.sendMessage("§6List of Mobs:");
                    for (EntityType e : EntityType.values())
                        if (e != null)
                            sender.sendMessage(e.toString());
                } else if (args[0].equalsIgnoreCase("setloot") && player != null) {
                    setItem(player.getInventory().getItemInMainHand(), "loot." + args[1], lootFile);
                    sender.sendMessage("§eSet loot at index " + args[1] + " §eto item in hand.");
                } else {
                    throwError(sender);
                }
            }
        } catch (Exception x) {
            throwError(sender);
            x.printStackTrace();
        }
        return true;
    }

    private void throwError(CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reload");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im setloot <index>");
        sender.sendMessage("Usage: /im giveloot <player> <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im showAbilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
        sender.sendMessage("Usage: /im purge");
    }

    void saveAsync(FileConfiguration config, File file) {
        var copied = new YamlConfiguration();

        for (var key : config.getKeys(false)) {
            var value = config.get(key, null);
            copied.set(key, value);
        }

        ForkJoinPool.commonPool().execute(() -> {
            try {
                copied.save(file);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Could not save " + file.getName() + ".", e);
            }
        });
    }

    private EntityType getEntityTypeFromName(String name) {
        for (var type : EntityType.values()) {
            if (type != EntityType.UNKNOWN && type.getKey().getKey().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    private void addNewInfernalMobToMap(InfernalMob newMob) {
        infernalMobMap.put(newMob.id, newMob);
    }
}