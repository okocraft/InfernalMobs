package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
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
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
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

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class InfernalMobsPlugin extends JavaPlugin implements Listener {

    GUI gui;
    long serverTime = 0L;
    private int loops;
    List<InfernalMob> infernalList = new ArrayList<>();
    private final List<UUID> droppedLootList = new ArrayList<>();
    private File lootYML = new File(getDataFolder(), "loot.yml");
    File saveYML = new File(getDataFolder(), "save.yml");
    private YamlConfiguration lootFile = YamlConfiguration.loadConfiguration(lootYML);
    YamlConfiguration mobSaveFile = YamlConfiguration.loadConfiguration(saveYML);
    private final HashMap<Entity, Entity> mountList = new HashMap();
    List<Player> errorList = new ArrayList<>();
    List<Player> levitateList = new ArrayList<>();

    public void onEnable() {
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
                System.out.println(Bukkit.getVersion() + " contains " + getConfig().getString("configVersion"));
                getLogger().log(Level.INFO, "Old config found, deleting!");
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
        if (Bukkit.getVersion().contains("1.16")) {
            configVersion = "1_16";
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
                getConfig().set("configVersion", Bukkit.getVersion());
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
        //Register Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        applyEffect();
        reloadPowers();
        showEffect();
        addRecipes();
    }

    private void reloadPowers() {
        List<World> wList = new ArrayList<>();
        for (Player p : getServer().getOnlinePlayers()) {
            if (!wList.contains(p.getWorld())) {
                wList.add(p.getWorld());
            }
        }
        for (World world : wList) {
            giveMobsPowers(world);
        }
    }

    private void scoreCheck() {
        for (Player p : getServer().getOnlinePlayers())
            GUI.fixBar(p);
        HashMap<Entity, Entity> tmp = (HashMap<Entity, Entity>) mountList.clone();
        for (Map.Entry<Entity, Entity> hm : tmp.entrySet()) {
            if ((hm.getKey() != null) && (!hm.getKey().isDead())) {
                if ((hm.getValue().isDead()) && ((hm.getKey() instanceof LivingEntity))) {
                    String fate = getConfig().getString("mountFate", "nothing");
                    if (fate.equals("death")) {
                        LivingEntity le = (LivingEntity) hm.getKey();
                        le.damage(9.99999999E8D);
                        mountList.remove(hm.getKey());
                    } else if (fate.equals("removal")) {
                        hm.getKey().remove();
                        getLogger().log(Level.INFO, "Entity remove due to Fate");
                        mountList.remove(hm.getKey());
                    }
                }
            } else {
                mountList.remove(hm.getKey());
            }
        }
    }

    void giveMobsPowers(World world) {
        for (Entity ent : world.getEntities()) {
            if (((ent instanceof LivingEntity)) && (mobSaveFile.getString(ent.getUniqueId().toString()) != null)) {
                giveMobPowers(ent);
            }
        }
    }

    void giveMobPowers(Entity ent) {
        UUID id = ent.getUniqueId();
        if (idSearch(id) == -1) {
            List<String> aList = null;
            for (MetadataValue v : ent.getMetadata("infernalMetadata")) {
                aList = new ArrayList(Arrays.asList(v.asString().split(",")));
            }
            if (aList == null) {
                if (mobSaveFile.getString(ent.getUniqueId().toString()) != null) {
                    aList = new ArrayList(Arrays.asList(mobSaveFile.getString(ent.getUniqueId().toString(), "").split(",")));
                    String list = getPowerString(ent, aList);
                    ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
                } else {
                    aList = getAbilitiesAmount(ent);
                }
            }
            InfernalMob newMob;
            if (aList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, aList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, aList, 1, getEffect());
            }
            if (aList.contains("flying")) {
                makeFly(ent);
            }
            infernalList.add(newMob);
        }
    }

    void makeInfernal(final Entity e, final boolean fixed) {
        String entName = e.getType().name();
        if ((!e.hasMetadata("NPC")) && (!e.hasMetadata("shopkeeper"))) {
            if (!fixed) {
                List<String> babyList = (ArrayList) getConfig().getList("disabledBabyMobs", new ArrayList<>());
                if (e instanceof Ageable) {
                    Ageable age = (Ageable) e;
                    boolean baby = !age.isAdult();
                    if (baby && babyList.contains(entName)) {
                        return;
                    }
                }
            }
            final UUID id = e.getUniqueId();
            final int chance = getConfig().getInt("chance");
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                String entName1 = e.getType().name();
                if ((!e.isDead()) && (e.isValid()) && (
                        ((getConfig().getStringList("enabledmobs").contains(entName1))) || ((fixed) &&
                                (idSearch(id) == -1)))) {
                    //Default
                    int min = 1;
                    int max = chance;
                    //Pe InfernalMob
                    int mc = getConfig().getInt("mobChances." + entName1);
                    if (mc > 0)
                        max = mc;
                    if (fixed)
                        max = 1;
                    //int randomNum = new Random().nextInt(max - min) + min;
                    int randomNum = rand(min, max);
                    if (randomNum == 1) {
                        List<String> aList = getAbilitiesAmount(e);
                        if (getConfig().getString("levelChance." + aList.size()) != null) {
                            int sc = getConfig().getInt("levelChance." + aList.size());
                            int randomNum2 = new Random().nextInt(sc - min) + min;
                            if (randomNum2 != 1) {
                                return;
                            }
                        }
                        InfernalMob newMob;
                        if (aList.contains("1up")) {
                            newMob = new InfernalMob(e, id, true, aList, 2, getEffect());
                        } else {
                            newMob = new InfernalMob(e, id, true, aList, 1, getEffect());
                        }

                        //fire event
                        InfernalSpawnEvent infernalEvent = new InfernalSpawnEvent(e, newMob);
                        Bukkit.getPluginManager().callEvent(infernalEvent);
                        if (infernalEvent.isCancelled()) {
                            return;
                        }

                        if (aList.contains("flying")) {
                            makeFly(e);
                        }
                        infernalList.add(newMob);
                        gui.setName(e);
                        giveMobGear(e, true);
                        addHealth(e, aList);
                        if (getConfig().getBoolean("enableSpawnMessages")) {
                            if (getConfig().getList("spawnMessages") != null) {
                                List<String> spawnMessageList = getConfig().getStringList("spawnMessages");
                                Random randomGenerator = new Random();
                                int index = randomGenerator.nextInt(spawnMessageList.size());
                                String spawnMessage = spawnMessageList.get(index);

                                spawnMessage = ChatColor.translateAlternateColorCodes('&', spawnMessage);
                                if (e.getCustomName() != null) {
                                    spawnMessage = spawnMessage.replace("mob", e.getCustomName());
                                } else {
                                    spawnMessage = spawnMessage.replace("mob", e.getType().toString().toLowerCase());
                                }
                                int r = getConfig().getInt("spawnMessageRadius");
                                if (r == -1) {
                                    for (Player p : e.getWorld().getPlayers()) {
                                        p.sendMessage(spawnMessage);
                                    }
                                } else if (r == -2) {
                                    Bukkit.broadcastMessage(spawnMessage);
                                } else {
                                    for (Entity e1 : e.getNearbyEntities(r, r, r)) {
                                        if ((e1 instanceof Player)) {
                                            Player p = (Player) e1;
                                            p.sendMessage(spawnMessage);
                                        }
                                    }
                                }
                            } else {
                                System.out.println("No valid spawn messages found!");
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    private void addHealth(Entity ent, List<String> powerList) {
        //double maxHealth = ((org.bukkit.entity.Damageable) ent).getHealth();
        AttributeInstance attributeMaxHealth = ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeMaxHealth == null) {
            return;
        }
        double maxHealth = attributeMaxHealth.getBaseValue();
        float setHealth;
        if (getConfig().getBoolean("healthByPower")) {
            int mobIndex = idSearch(ent.getUniqueId());
            try {
                InfernalMob m = infernalList.get(mobIndex);
                setHealth = (float) (maxHealth * m.abilityList.size());
            } catch (Exception e) {
                setHealth = (float) (maxHealth * 5.0D);
            }
        } else {
            if (getConfig().getBoolean("healthByDistance")) {
                Location l = ent.getWorld().getSpawnLocation();
                int m = (int) l.distance(ent.getLocation()) / getConfig().getInt("addDistance");
                if (m < 1) {
                    m = 1;
                }
                int add = getConfig().getInt("healthToAdd");
                setHealth = m * add;
            } else {
                int healthMultiplier = getConfig().getInt("healthMultiplier");
                setHealth = (float) (maxHealth * healthMultiplier);
            }
        }
        if (setHealth >= 1.0F) {
            try {
                attributeMaxHealth.setBaseValue(setHealth);
                ((LivingEntity) ent).setHealth(setHealth);
            } catch (Exception e) {
                System.out.println("addHealth: " + e);
            }
        }
        String list = getPowerString(ent, powerList);
        ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
        try {
            mobSaveFile.set(ent.getUniqueId().toString(), list);
            mobSaveFile.save(saveYML);
        } catch (IOException ignored) {
        }
    }

    private String getPowerString(Entity ent, List<String> powerList) {
        StringBuilder list = new StringBuilder();
        for (String s : powerList) {
            if (powerList.indexOf(s) != powerList.size() - 1) {
                list.append(s).append(",");
            } else {
                list.append(s);
            }
        }
        return list.toString();
    }

    void removeMob(int mobIndex) throws IOException {
        String id = infernalList.get(mobIndex).id.toString();
        infernalList.remove(mobIndex);
        mobSaveFile.set(id, null);
        mobSaveFile.save(saveYML);
    }

    void spawnGhost(Location l) {
        boolean evil = new Random().nextInt(3) == 1;
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
        chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, new Random().nextInt(10) + 1);
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
        int min = 1;
        int max = 5;
        int rn = new Random().nextInt(max - min) + min;
        if (rn == 1) {
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
        infernalList.add(newMob);
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

    void keepAlive(Item item) {
        final UUID id = item.getUniqueId();
        droppedLootList.add(id);
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> droppedLootList.remove(id), 300L);
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
            System.out.println("Error in get random loot ");
            e.printStackTrace();
            System.out.println("Error: No valid drops found!");
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
        //System.out.println("Get Loot: " + loot);
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
                    if (title != null) {
                        title = ChatColor.translateAlternateColorCodes('&', title);
                        bMeta.setTitle(title);
                    }
                    ConfigurationSection lootPages = lootFile.getConfigurationSection("loot." + loot + ".pages");
                    if (lootPages != null) {
                        for (String i : lootPages.getKeys(false)) {
                            if (lootFile.isString("loot." + loot + ".pages." + i)) {
                                String page = lootFile.getString("loot." + loot + ".pages." + i);
                                page = ChatColor.translateAlternateColorCodes('&', page);
                                bMeta.addPage(page);
                            }
                        }
                    }
                    stack.setItemMeta(bMeta);
                }
                //Banners
                if (meta instanceof BannerMeta) {
                    BannerMeta b = (BannerMeta) meta;
                    List<Pattern> patList = (List<Pattern>) lootFile.getList("loot." + loot + ".patterns");
                    if (patList != null && (!patList.isEmpty()))
                        b.setPatterns(patList);
                    stack.setItemMeta(b);
                }
                //Shield
                if (stack.getType() == Material.SHIELD) {
                    BlockStateMeta bmeta = (BlockStateMeta) meta;

                    Banner b = (Banner) bmeta.getBlockState();
                    List<Pattern> patList = (List<Pattern>) lootFile.getList("loot." + loot + ".patterns");
                    b.setBaseColor(DyeColor.valueOf(lootFile.getString("loot." + loot + ".colour")));
                    b.setPatterns(patList);
                    b.update();
                    bmeta.setBlockState(b);
                    stack.setItemMeta(bmeta);
                }
                //Owner
                if (meta instanceof SkullMeta && lootFile.isString("loot." + loot + ".owner")) {
                    SkullMeta sm = (SkullMeta) meta;
                    String owner = lootFile.getString("loot." + loot + ".owner");
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
                    stack.setItemMeta(sm);
                }
                //Potions
                if (lootFile.getString("loot." + loot + ".potion") != null && meta instanceof PotionMeta) {
                    PotionMeta pMeta = (PotionMeta) meta;
                    String pn = lootFile.getString("loot." + loot + ".potion");
                    pMeta.setBasePotionData(new PotionData(PotionType.valueOf(pn), false, false));
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
                    //int enchNeeded = new Random().nextInt(enMax + 1 - enMin) + enMin;
                    int enchNeeded = rand(enMin, enMax);
                    List<LevelledEnchantment> enchList = new ArrayList<>();
                    int safety = 0;
                    int j = 0;
                    int chance;
                    do {
                        if (lootFile.getString("loot." + loot + ".enchantments." + j) != null) {
                            int enChance = 1;
                            if (lootFile.getString("loot." + loot + ".enchantments." + j + ".chance") != null) {
                                enChance = lootFile.getInt("loot." + loot + ".enchantments." + j + ".chance");
                            }
                            chance = new Random().nextInt(enChance - 1 + 1) + 1;
                            if (chance == 1) {
                                String enchantment = lootFile.getString("loot." + loot + ".enchantments." + j + ".enchantment").toLowerCase();
                                String levelString = lootFile.getString("loot." + loot + ".enchantments." + j + ".level");
                                int level = getIntFromString(levelString);
                                //System.out.print("1: " + NamespacedKey.minecraft(enchantment));
                                //System.out.print("2: " + Enchantment.getByKey(NamespacedKey.minecraft(enchantment)));
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
                                    System.out.println("Error: No valid drops found!");
                                    System.out.println("Error: " + enchantment + " is not a valid enchantment!");
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
                            //System.out.println("Error: No valid drops found!");
                            //System.out.println("Error: Please increase chance for enchantments on item " + loot);
                            //return null;
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
                if (b != null) {
                    List patList = b.getPatterns();
                    if (!patList.isEmpty())
                        fc.set(path + ".patterns", patList);
                }
            }
            //Shield
            if (s.getType() == Material.SHIELD) {
                ItemMeta im = meta;
                BlockStateMeta bmeta = (BlockStateMeta) im;
                Banner b = (Banner) bmeta.getBlockState();

                fc.set(path + ".colour", b.getBaseColor().toString());
                List patList = b.getPatterns();
                if (!patList.isEmpty())
                    fc.set(path + ".patterns", patList);
            }
            //Potions
            if (meta instanceof PotionMeta) {
                PotionMeta pMeta = (PotionMeta) meta;
                org.bukkit.potion.PotionData pd = pMeta.getBasePotionData();
                fc.set(path + ".potion", pd.getType().getEffectType().getName());
            }
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta l = (LeatherArmorMeta) meta;
                Color c = l.getColor();
                String color = c.getRed() + "," + c.getGreen() + "," + c.getBlue();
                fc.set(path + ".colour", color);
            }
            if (meta instanceof SkullMeta) {
                SkullMeta sm = (SkullMeta) meta;
                fc.set(path + ".owner", sm.getOwningPlayer().getUniqueId().toString());
            }
            List<String> flags = new ArrayList<>();
            for (ItemFlag f : meta.getItemFlags())
                if (f != null)
                    flags.add(f.name());
            if (!flags.isEmpty())
                fc.set(path + ".flags", flags);
        } else {
            System.out.println("Item is null!");
        }
        try {
            lootFile.save(lootYML);
        } catch (IOException ignored) {
        }
        saveConfig();
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
                    setAmount = new Random().nextInt(maxSetAmount - minSetAmount + 1) + minSetAmount;
                } catch (Exception e) {
                    System.out.println("getIntFromString: " + e);
                }
            } else {
                setAmount = Integer.parseInt(setAmountString);
            }
        } catch (Exception ignored) {
        }
        return setAmount;

    }

    private boolean isBaby(Entity mob) {
        if (mob instanceof Ageable) {
            return !((Ageable) mob).isAdult();
        }
        return false;
    }

    private String getEffect() {
        String effect = "mobSpawnerFire";
        try {
            //Get Enabled Particles
            List<String> partTypes = getConfig().getStringList("mobParticles");
            //Get Random Particle
            effect = partTypes.get(new Random().nextInt(partTypes.size()));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return effect;
    }

    private void displayEffect(Location l, String effect) {
        if (effect == null) {
            try {
                //Get Particles
                effect = getEffect();
            } catch (Exception e) {
                effect = "mobSpawnerFire";
            }
        }
        //Get Effect and Data
        String[] split = effect.split(":");
        effect = split[0];
        int data1 = Integer.parseInt(split[1]);
        int data2 = Integer.parseInt(split[2]);
        try {
            String f = "FLAME";
            switch (effect) {
                case "potionBrake":
                    f = Particle.SPELL.toString();
                    break;
                case "smoke":
                    f = Particle.SMOKE_NORMAL.toString();
                    break;
                case "blockBrake":
                    f = Particle.BLOCK_CRACK.toString();
                    break;
                case "hugeExplode":
                    f = Particle.EXPLOSION_HUGE.toString();
                    break;
                case "angryVillager":
                    f = Particle.VILLAGER_ANGRY.toString();
                    break;
                case "cloud":
                    f = Particle.CLOUD.toString();
                    break;
                case "criticalHit":
                    f = Particle.CRIT.toString();
                    break;
                case "mobSpell":
                    f = Particle.SPELL_MOB.toString();
                    break;
                case "enchantmentTable":
                    f = Particle.ENCHANTMENT_TABLE.toString();
                    break;
                case "ender":
                    f = Particle.PORTAL.toString();
                    break;
                case "explode":
                    f = Particle.EXPLOSION_NORMAL.toString();
                    break;
                case "greenSparkle":
                    f = Particle.VILLAGER_HAPPY.toString();
                    break;
                case "heart":
                    f = Particle.HEART.toString();
                    break;
                case "largeExplode":
                    f = Particle.EXPLOSION_LARGE.toString();
                    break;
                case "splash":
                    f = Particle.WATER_SPLASH.toString();
                    break;
                case "largeSmoke":
                    f = Particle.SMOKE_LARGE.toString();
                    break;
                case "lavaSpark":
                    f = Particle.LAVA.toString();
                    break;
                case "magicCriticalHit":
                    f = Particle.CRIT_MAGIC.toString();
                    break;
                case "noteBlock":
                    f = Particle.NOTE.toString();
                    break;
                case "tileDust":
                    f = Particle.BLOCK_DUST.toString();
                    break;
                case "colouredDust":
                    f = Particle.REDSTONE.toString();
                    break;
                case "flame":
                    f = Particle.FLAME.toString();
                    break;
                case "witchMagic":
                    f = Particle.SPELL_WITCH.toString();
                    break;
            }
            if (f != null) {
                displayParticle(f, l, 1.0, data1, data2);
            } else
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, data2);
        } catch (Exception x) {
            //x.printStackTrace();
        }
    }

    private void showEffect() {
        try {
            //GUI Bars And Stuff
            scoreCheck();
            //InfernalMob Stuff
            List<InfernalMob> tmp = new ArrayList<>(infernalList);
            for (InfernalMob m : tmp) {
                final Entity mob = m.entity;
                UUID id = mob.getUniqueId();
                int index = idSearch(id);
                if (mob.isValid() && (!mob.isDead()) && (index != -1) && (mob.getLocation().getChunk().isLoaded())) {
                    //System.out.println("PE2");
                    Location feet = mob.getLocation();
                    Location head = mob.getLocation();
                    head.setY(head.getY() + 1);
                    if (getConfig().getBoolean("enableParticles")) {
                        displayEffect(feet, m.effect);
                        //mob.getWorld().playEffect(feet, Effect.ENDER_SIGNAL, 1);
                        if (!isSmall(mob)) {
                            displayEffect(head, m.effect);
                            //mob.getWorld().playEffect(head, Effect.ENDER_SIGNAL, 1);
                        }
                        if (mob.getType() == EntityType.ENDERMAN || mob.getType() == EntityType.IRON_GOLEM) {
                            head.setY(head.getY() + 1);
                            displayEffect(head, m.effect);
                            //mob.getWorld().playEffect(head, Effect.ENDER_SIGNAL, 1);
                        }
                    }
                    //Ability's
                    List<String> abilityList = findMobAbilities(id);
                    //System.out.println("PE1");
                    if (!mob.isDead()) {
                        for (String ability : abilityList) {
                            Random rand = new Random();
                            int min = 1;
                            int max = 10;
                            int randomNum = rand.nextInt(max - min) + min;
                            //System.out.println("PE: " + ability);
                            switch (ability) {
                                case "cloaked":
                                    ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1), true);
                                    break;
                                case "armoured":
                                    if ((!(mob instanceof Skeleton)) && (!(mob instanceof Zombie))) {
                                        ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1), true);
                                    }
                                    break;
                                case "1up":
                                    if (((org.bukkit.entity.Damageable) mob).getHealth() <= 5) {
                                        InfernalMob oneUpper = infernalList.get(index);
                                        if (oneUpper.lives > 1) {
                                            //System.out.print("1");//-------------------------------Debug
                                            // ((org.bukkit.entity.Damageable) mob).setHealth(((org.bukkit.entity.Damageable) mob).);

                                            //System.out.print("UP!");//-------------------------------Debug
                                            //InfernalMob newMob = new InfernalMob(mob, id, mob.getWorld(), oneUpper.infernal, abilityList, 1, getEffect());
                                            //infernalList.set(index, newMob);
                                            ((LivingEntity) mob).setHealth(((LivingEntity) mob).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                                            oneUpper.setLives(oneUpper.lives - 1);
                                        }
                                    }
                                    break;
                                case "sprint":
                                    ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
                                    break;
                                case "molten":
                                    ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1), true);
                                    break;
                                case "tosser":
                                    if (randomNum < 6) {
                                        double radius = 6D;
                                        List<Player> near = mob.getWorld().getPlayers();
                                        for (Player player : near) {
                                            if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                                if (!player.isSneaking() && player.getGameMode() != GameMode.CREATIVE) {
                                                    player.setVelocity(mob.getLocation().toVector().subtract(player.getLocation().toVector()));
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "gravity":
                                    if (randomNum >= 9) {
                                        double radius = 10D;
                                        List<Player> near = mob.getWorld().getPlayers();
                                        for (Player player : near) {
                                            if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                                Location feetBlock = player.getLocation();
                                                feetBlock.setY(feetBlock.getY() - 2);
                                                Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                                                if (block.getType() != Material.AIR && player.getGameMode() != GameMode.CREATIVE) {
                                                    int amount = 6;
                                                    if (getConfig().getString("gravityLevitateLength") != null) {
                                                        amount = getConfig().getInt("gravityLevitateLength");
                                                    }
                                                    levitate(player, amount);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "ghastly":
                                case "necromancer":
                                    if ((randomNum == 6) && (!mob.isDead())) {
                                        double radius = 20D;
                                        List<Player> near = mob.getWorld().getPlayers();
                                        for (Player player : near) {
                                            if (player.getLocation().distance(mob.getLocation()) <= radius && player.getGameMode() != GameMode.CREATIVE) {
                                                Fireball fb;
                                                if (ability.equals("ghastly")) {
                                                    fb = ((LivingEntity) mob).launchProjectile(Fireball.class);
                                                    player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                                                } else {
                                                    fb = ((LivingEntity) mob).launchProjectile(WitherSkull.class);
                                                }
                                                //Location loc1 = player.getEyeLocation();
                                                //Location loc2 = mob.getLocation();
                                                //int arrowSpeed = 1;
                                                //loc2.setY(loc2.getBlockY()+2);
                                                //loc2.setX(loc2.getBlockX()+0.5);
                                                //loc2.setZ(loc2.getBlockZ()+0.5);
                                                //Arrow ar = mob.getWorld().spawnArrow(loc2, new Vector(loc1.getX()-loc2.getX(), loc1.getY()-loc2.getY(), loc1.getZ()-loc2.getZ()), arrowSpeed, 12);
                                                //Vector vel = ar.getVelocity();
                                                //fb.setVelocity(vel);
                                                //ar.remove();
                                                moveToward(fb, player.getLocation(), 0.6);
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        serverTime = serverTime + 1;
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::showEffect, 20);
    }

    public boolean isSmall(Entity mob) {
        switch (mob.getType()) {
            case BAT:
            case CAVE_SPIDER:
            case CHICKEN:
            case COW:
            case MUSHROOM_COW:
            case PIG:
            case OCELOT:
            case SHEEP:
            case SILVERFISH:
            case SPIDER:
            case WOLF:
                return true;
            default:
                return isBaby(mob);
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
        //Check Players
        for (Player p : getServer().getOnlinePlayers()) {
            World world = p.getWorld();
            if (getConfig().getStringList("effectworlds").contains(world.getName()) || (getConfig().getStringList("effectworlds").contains("<all>"))) {
                HashMap<Integer, ItemStack> itemMap = new HashMap<>();
                for (int i : getConfig().getIntegerList("enabledCharmSlots")) {
                    ItemStack in;
                    in = p.getInventory().getItem(i);
                    itemMap.put(i, in);
                }
                int ai = 100;
                for (ItemStack ar : p.getInventory().getArmorContents())
                    if (ar != null) {
                        itemMap.put(ai, ar);
                        ai = ai + 1;
                    }
                //for(int i = 0; i < 256; i++){
                if (lootFile.getString("potionEffects") != null)
                    for (String id : lootFile.getConfigurationSection("potionEffects").getKeys(false))
                        if ((lootFile.getString("potionEffects." + id) != null) && (lootFile.getString("potionEffects." + id + ".attackEffect") == null) && (lootFile.getString("potionEffects." + id + ".attackHelpEffect") == null)) {
                            List<ItemStack> itemsPlayerHas = new ArrayList<>();
                            for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + id + ".requiredItems")) {
                                ItemStack neededItem = getItem(neededItemIndex);
                                for (Map.Entry<Integer, ItemStack> hm : itemMap.entrySet()) {
                                    ItemStack check = hm.getValue();
                                    try {
                                        if ((neededItem.getItemMeta() == null) || (check.getItemMeta().getDisplayName().equals(neededItem.getItemMeta().getDisplayName()))) {
                                            if (check.getType().equals(neededItem.getType())) {
                                                //if ((neededItem.getType().getMaxDurability() > 0) || ((Damageable)check).getDamage() == (((Damageable)neededItem).getDamage())) {
                                                if (!isArmor(neededItem) || hm.getKey() >= 100)
                                                    itemsPlayerHas.add(neededItem);
                                                //}
                                                //}
                                            }
                                        }
                                    } catch (Exception e) {/*System.out.println("Error: " + e);**/}
                                }
                            }

                            if (itemsPlayerHas.size() >= lootFile.getIntegerList("potionEffects." + id + ".requiredItems").size()) {
                                applyEffects(p, Integer.parseInt(id));
                            }
                        }
            }
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::applyEffect, (10 * 20));
    }

    private boolean isArmor(ItemStack s) {
        String t = s.getType().toString().toLowerCase();
        return t.contains("helm") || t.contains("plate") || t.contains("leg") || t.contains("boot");
    }

    private void applyEffects(LivingEntity e, int effectID) {
        int level = lootFile.getInt("potionEffects." + effectID + ".level");
        String name = lootFile.getString("potionEffects." + effectID + ".potion");
        if ((PotionEffectType.getByName(name) == PotionEffectType.HARM) || (PotionEffectType.getByName(name) == PotionEffectType.HEAL)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.getByName(name), 1, level - 1), true);
        } else {
            e.addPotionEffect(new PotionEffect(PotionEffectType.getByName(name), 400, level - 1), true);
        }
        if (lootFile.getString("potionEffects." + effectID + ".particleEffect") != null) {
            String effect = lootFile.getString("potionEffects." + effectID + ".particleEffect");
            showEffectParticles(e, effect, 15);
        }
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
                            } catch (Exception e) {/**System.out.println("Error: " + e);**/}
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
                                } catch (Exception e) {/**System.out.println("Error: " + e);**/}
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
            if (idSearch(id) != -1) {
                List<String> abilityList = findMobAbilities(id);
                if ((!player.isDead()) && (!mob.isDead())) {
                    for (String ability : abilityList)
                        doMagic(player, mob, isPlayerVictim, ability, id);
                }
            }
        } catch (Exception e) {/**System.out.println("Do Effect Error: " + e);**/}
    }

    private void doMagic(Entity vic, Entity atc, boolean isPlayerVictim, String ability, UUID id) {
        int min = 1;
        int max = 10;
        int randomNum = new Random().nextInt(max - min) + min;
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
                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
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
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1), true);
            } else if ((ability.equals("morph")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                try {
                    Entity newEnt;
                    int mc = new Random().nextInt(25) + 1;
                    if (mc != 20) {
                        return;
                    }
                    Location l = atc.getLocation().clone();
                    double h = ((org.bukkit.entity.Damageable) atc).getHealth();
                    List<String> aList = infernalList.get(idSearch(id)).abilityList;
                    //Remove old
                    double dis = 46.0D;
                    for (Entity e : atc.getNearbyEntities(dis, dis, dis))
                        if (e instanceof Player)
                            GUI.fixBar(((Player) e));
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0D, l.getZ()));
                    atc.remove();
                    getLogger().log(Level.INFO, "Entity remove due to Morph");
                    List<String> mList = getConfig().getStringList("enabledmobs");
                    int index = new Random().nextInt(mList.size());
                    String mobName = mList.get(index);

                    newEnt = null;
                    EntityType[] arrayOfEntityType;
                    int j = (arrayOfEntityType = EntityType.values()).length;
                    for (int i = 0; i < j; i++) {
                        EntityType e = arrayOfEntityType[i];
                        try {
                            if ((e.getName() != null) && (e.getName().equalsIgnoreCase(mobName))) {
                                newEnt = vic.getWorld().spawnEntity(l, e);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (newEnt == null) {
                        System.out.println("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    InfernalMob newMob;
                    if (aList.contains("1up")) {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 2, getEffect());
                    } else {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 1, getEffect());
                    }
                    if (aList.contains("flying")) {
                        makeFly(newEnt);
                    }
                    infernalList.set(idSearch(id), newMob);
                    gui.setName(newEnt);

                    giveMobGear(newEnt, true);

                    addHealth(newEnt, aList);
                    if (h >= ((LivingEntity) newEnt).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        return;
                    }
                    ((org.bukkit.entity.Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    System.out.print("Morph Error: ");
                    ex.printStackTrace();
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
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
            } else if ((ability.equals("confusing")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 2), true);
            } else if ((ability.equals("withering")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1), true);
            } else if ((ability.equals("thief")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                if ((vic instanceof Player)) {
                    if ((((Player) vic).getInventory().getItemInMainHand().getType() != Material.AIR) && ((randomNum <= 1) || (randomNum == 1))) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInMainHand());
                        int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, null);
                    }
                } else if (vic instanceof Zombie || vic instanceof Skeleton) {
                    EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInMainHand());
                    eq.setItemInMainHand(null);
                }
            } else if ((ability.equals("quicksand")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 1), true);
            } else if ((ability.equals("bullwark")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 2), true);
            } else if ((ability.equals("rust")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ItemStack damItem = ((Player) vic).getInventory().getItemInMainHand();
                if (randomNum <= 3 && damItem.getMaxStackSize() == 1) {
                    int cDur = ((Damageable) damItem.getItemMeta()).getDamage();
                    ((Damageable) damItem.getItemMeta()).setDamage(cDur + 20);
                }
            } else if ((ability.equals("sapper")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1), true);
            } else if ((!ability.equals("1up")) || (!isLegitVictim(atc, isPlayerVictim, ability))) {
                Location needAir2;
                if ((ability.equals("ender")) && (isLegitVictim(atc, isPlayerVictim, ability))) {
                    Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        Random rand2 = new Random();
                        int min2 = 1;
                        int max2 = 4;
                        int randomNum2 = rand2.nextInt(max2 - min2 + 1) + min2;
                        if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0D);
                        } else if (randomNum2 == 2) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0D);
                        } else if (randomNum2 == 3) {
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
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1), true);
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

                                int rNum = new Random().nextInt(max - min) + min;
                                if ((rNum == 5) && (
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
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1), true);
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
                            PotionMeta potion = (PotionMeta) iStack.getItemMeta();
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
                                if (!isSmall(atc)) {
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
                            LeatherArmorMeta tmpCol2 = (LeatherArmorMeta) tmpCol.getItemMeta();
                            tmpCol2.setColor(Color.fromRGB(red, green, blue));

                            Color col = tmpCol2.getColor();
                            launchFirework(atc.getLocation(), col, 1);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static List<Block> getSphere(Block block1) {
        List<Block> blocks = new LinkedList();
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

    private void launchFirework(Location l, Color c, int speed) {
        Firework fw = l.getWorld().spawn(l, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
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
        int min = 1;
        for (int i = 0; i < amount; i++) {
            int max = allAbilitiesList.size();
            int randomNum = new Random().nextInt(max - min) + min;
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

    public int idSearch(UUID id) {
        InfernalMob idMob = null;
        for (InfernalMob mob : infernalList) {
            if (mob.id.equals(id)) {
                idMob = mob;
            }
        }
        if (idMob != null) {
            return infernalList.indexOf(idMob);
        }
        return -1;
    }

    public List<String> findMobAbilities(UUID id) {
        for (InfernalMob mob : infernalList) {
            if (mob.id.equals(id)) {
                return mob.abilityList;
            }
        }
        return null;
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
        //bat.setPassenger(ent);
        bat.addPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1), true);
    }

    private void giveMobGear(Entity mob, boolean naturalSpawn) {
        UUID mobId = mob.getUniqueId();
        List<String> mobAbilityList = null;
        boolean armoured = false;
        if (idSearch(mobId) != -1) {
            mobAbilityList = findMobAbilities(mobId);
            if (mobAbilityList.contains("armoured")) {
                armoured = true;
                ((LivingEntity) mob).setCanPickupItems(false);
            }
        }
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET, 1);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        EntityEquipment ee = ((LivingEntity) mob).getEquipment();
        if (mob.getType() == EntityType.WITHER_SKELETON) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                ee.setLeggings(pants);
                ee.setBoots(boots);
                ee.setItemInMainHand(sword);
            }
        } else if (mob.getType() == EntityType.SKELETON) {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            ee.setItemInMainHand(bow);
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggingsDropChance(0.0F);
                    ee.setBootsDropChance(0.0F);
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                ee.setHelmet(skull);
            }
        } else if (mob instanceof Zombie) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE);
                //skull.setDurability((short) 2);
                ee.setHelmet(skull);
            }
        }
        if (((mobAbilityList.contains("mounted")) && (getConfig().getStringList("enabledRiders").contains(mob.getType().name()))) || ((!naturalSpawn) && (mobAbilityList.contains("mounted")))) {
            List<String> mounts;

            mounts = getConfig().getStringList("enabledMounts");

            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(mounts.size());
            String mount = mounts.get(index);
            //Type
            String type = null;
            if (mount.contains(":")) {
                String[] s = mount.split(":");
                mount = s[0];
                type = s[1];
            }
            if (EntityType.fromName(mount) != null && (EntityType.fromName(mount) != EntityType.ENDER_DRAGON)) {
                Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), EntityType.fromName(mount));

                mountList.put(liveMount, mob);
                liveMount.addPassenger(mob);
                if (liveMount.getType() == EntityType.HORSE) {
                    Horse hm = (Horse) liveMount;
                    if (getConfig().getBoolean("horseMountsHaveSaddles")) {
                        ItemStack saddle = new ItemStack(Material.SADDLE);
                        hm.getInventory().setSaddle(saddle);
                    }
                    hm.setTamed(true);
                    int randomNum3 = rand(1, 7);
                    if (randomNum3 == 1) {
                        hm.setColor(Horse.Color.BLACK);
                    } else if (randomNum3 == 2) {
                        hm.setColor(Horse.Color.BROWN);
                    } else if (randomNum3 == 3) {
                        hm.setColor(Horse.Color.CHESTNUT);
                    } else if (randomNum3 == 4) {
                        hm.setColor(Horse.Color.CREAMY);
                    } else if (randomNum3 == 5) {
                        hm.setColor(Horse.Color.DARK_BROWN);
                    } else if (randomNum3 == 6) {
                        hm.setColor(Horse.Color.GRAY);
                    } else {
                        hm.setColor(Horse.Color.WHITE);
                    }
                    if ((armoured) && (getConfig().getBoolean("armouredMountsHaveArmour"))) {
                        ItemStack armour = new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1);
                        hm.getInventory().setArmor(armour);
                    }
                } else if (liveMount.getType() == EntityType.SHEEP) {
                    Sheep sh = (Sheep) liveMount;
                    if (type != null) {
                        sh.setColor(DyeColor.valueOf(type));
                    }
                }
            } else {
                System.out.println("Can't spawn mount!");
                System.out.println(mount + " is not a valid Entity!");
            }
        }
    }

    private void displayParticle(String effect, Location l, double radius, int speed, int amount) {
        displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
    }

    void displayParticle(String effect, World w, double x, double y, double z, double radius, int speed, int amount) {
        amount = (amount <= 0) ? 1 : amount;
        Location l = new Location(w, x, y, z);
        try {
            if (radius <= 0) {
                w.spawnParticle(Particle.valueOf(effect), l, 0, 0, 0, speed, amount);
            } else {
                List<Location> ll = getArea(l, radius, 0.2);
                if (ll.size() > 0) {
                    for (int i = 0; i < amount; i++) {
                        int index = new Random().nextInt(ll.size());
                        w.spawnParticle(Particle.valueOf(effect), ll.get(index), 1, 0, 0, 0, 0);
                        ll.remove(index);
                    }
                }
            }
        } catch (Exception ex) {
            // System.out.println("V: " + getServer().getVersion());
            // ex.printStackTrace();
        }
    }

    private List<Location> getArea(Location l, double r, double t) {
        List<Location> ll = new ArrayList<>();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
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

    Block blockNear(Location l, Material mat, int radius) {
        double xTmp = l.getX();
        double yTmp = l.getY();
        double zTmp = l.getZ();
        int finalX = (int) Math.round(xTmp);
        int finalY = (int) Math.round(yTmp);
        int finalZ = (int) Math.round(zTmp);
        for (int x = finalX - radius; x <= finalX + radius; x++) {
            for (int y = finalY - radius; y <= finalY + radius; y++) {
                for (int z = finalZ - radius; z <= finalZ + radius; z++) {
                    Location loc = new Location(l.getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType() == mat) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    private boolean cSpawn(CommandSender sender, String mob, Location l, List<String> abList) {
        //cspawn <mob> <world> <x> <y> <z> <ability> <ability>
        if ((EntityType.fromName(mob) != null)) {
            Entity ent = l.getWorld().spawnEntity(l, EntityType.fromName(mob));//
            InfernalMob newMob;
            UUID id = ent.getUniqueId();
            if (abList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());
            }
            if (abList.contains("flying")) {
                makeFly(ent);
            }
            infernalList.add(newMob);
            gui.setName(ent);

            giveMobGear(ent, false);
            addHealth(ent, abList);
            return true;
        } else {
            sender.sendMessage("Can't spawn a " + mob + "!");
            return false;
        }
    }

    private int rand(int min, int max) {
        return min + (int) (Math.random() * (1 + max - min));
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        Set<String> commands = new HashSet<>(Arrays.asList("reload", "worldInfo", "error", "getloot", "setloot", "giveloot", "abilities", "showAbilities", "setInfernal", "spawn", "cspawn", "pspawn", "kill", "killall"));
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
        ItemStack s = getItem(Material.BLAZE_ROD, "§6§lDivining Rod", 1, Collections.singletonList("Click to find infernal mobs."));
        ItemMeta m = s.getItemMeta();
        m.addEnchant(Enchantment.CHANNELING, 1, true);
        s.setItemMeta(m);
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

    private ItemStack getItem(Material mat, String name, int amount, List<String> loreList) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta m = item.getItemMeta();
        if (name != null)
            m.setDisplayName(name);
        if (loreList != null)
            m.setLore(loreList);
        item.setItemMeta(m);
        return item;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("infernalmobs")) || (cmd.getName().equalsIgnoreCase("im"))) {
            try {
                Player player = null;
                if (!(sender instanceof Player)) {
                    if (args != null && args.length > 0 && (!args[0].equalsIgnoreCase("cspawn")) && (!args[0].equalsIgnoreCase("pspawn")) && (!args[0].equalsIgnoreCase("giveloot")) && (!args[0].equalsIgnoreCase("reload")) && (!args[0].equalsIgnoreCase("killall"))) {
                        sender.sendMessage("This command can only be run by a player!");
                        return true;
                    }
                } else
                    player = (Player) sender;
                if (sender.hasPermission("infernal_mobs.commands")) {
                    if (args.length == 0) {
                        throwError(sender);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("slotTest")) {
                        for (int i : getConfig().getIntegerList("enabledCharmSlots"))
                            player.getInventory().setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("fixloot"))) {
                        List<String> list = new ArrayList<>(getConfig().getConfigurationSection("items").getKeys(false));
                        for (String i : lootFile.getConfigurationSection("loot").getKeys(false)) {
                            String oid = lootFile.getInt("loot." + i + ".item") + "";
                            System.out.println(i);
                            System.out.println("loot." + i + ".item");
                            System.out.println(oid + ": " + list.contains(oid));
                            if (list.contains(oid)) {
                                lootFile.set("loot." + i + ".item", getConfig().getString("items." + oid));
                            } else
                                System.out.println("ERROR: " + oid);
                        }
                        try {
                            lootFile.save(lootYML);
                        } catch (IOException ignored) {
                        }
                        sender.sendMessage("§eLoot Fixed!");
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
                        reloadConfig();
                        reloadLoot();
                        sender.sendMessage("§eConfig reloaded!");
                    } else if (args[0].equals("mobList")) {
                        sender.sendMessage("§6Mob List:");
                        for (EntityType et : EntityType.values())
                            if (et != null && et.getName() != null)
                                sender.sendMessage("§e" + et.getName());
                        return true;
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("error"))) {
                        errorList.add(player);
                        sender.sendMessage("§eClick on a mob to send an error report about it.");
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("info"))) {
                        sender.sendMessage("§eMounts: " + mountList.size());
                        sender.sendMessage("§eLoops: " + loops);
                        sender.sendMessage("§eInfernals: " + infernalList.size());
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("worldInfo"))) {
                        List<String> enWorldList = getConfig().getStringList("mobworlds");
                        World world = player.getWorld();
                        String enabled = "is not";
                        if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                            enabled = "is";
                        }
                        sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                        sender.sendMessage("All the worlds that are enabled are: " + enWorldList);
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("help"))) {
                        throwError(sender);
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("getloot"))) {
                        int min = getConfig().getInt("minpowers");
                        int max = getConfig().getInt("maxpowers");
                        int powers = rand(min, max);
                        ItemStack gottenLoot = getRandomLoot(player, getRandomMob(), powers);
                        if (gottenLoot != null) {
                            player.getInventory().addItem(gottenLoot);
                        }
                        sender.sendMessage("§eGave you some random loot!");
                    } else if ((args.length == 2) && (args[0].equalsIgnoreCase("getloot"))) {
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
                        if ((EntityType.fromName(args[1]) != null)) {
                            boolean exmsg;
                            World world;
                            Entity ent;
                            if ((args[0].equalsIgnoreCase("cspawn")) && (args[2] != null) && (args[3] != null) && (args[4] != null) && (args[5] != null)) {
                                if (getServer().getWorld(args[2]) == null) {
                                    sender.sendMessage(args[2] + " dose not exist!");
                                    return true;
                                }
                                world = getServer().getWorld(args[2]);
                                Location spoint = new Location(getServer().getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                                ent = world.spawnEntity(spoint, EntityType.fromName(args[1]));
                                exmsg = true;
                            } else {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                ent = player.getWorld().spawnEntity(farSpawnLoc, EntityType.fromName(args[1]));
                                exmsg = false;
                            }
                            List<String> abList = getAbilitiesAmount(ent);
                            InfernalMob newMob;
                            UUID id = ent.getUniqueId();
                            if (abList.contains("1up")) {
                                newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());
                            } else {
                                newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());
                            }

                            if (abList.contains("flying")) {
                                makeFly(ent);
                            }
                            infernalList.add(newMob);
                            gui.setName(ent);

                            giveMobGear(ent, false);
                            addHealth(ent, abList);
                            if (!exmsg) {
                                sender.sendMessage("Spawned a " + args[1]);
                            } else if (sender instanceof Player) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5]);
                            }
                        } else {
                            sender.sendMessage("Can't spawn a " + args[1] + "!");
                            return true;
                        }
                    } else if (((args.length >= 3) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length >= 6)) || ((args[0].equalsIgnoreCase("pspawn")) && (args.length >= 3))) {
                        if (args[0].equalsIgnoreCase("spawn")) {
                            if ((EntityType.fromName(args[1]) != null)) {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                Entity ent = player.getWorld().spawnEntity(farSpawnLoc, EntityType.fromName(args[1]));
                                List<String> spesificAbList = new ArrayList<>();
                                for (int i = 0; i <= args.length - 3; i++) {
                                    if (getConfig().getString(args[(i + 2)]) != null) {
                                        spesificAbList.add(args[(i + 2)]);
                                    } else {
                                        sender.sendMessage(args[(i + 2)] + " is not a valid ability!");
                                        return true;
                                    }
                                }
                                InfernalMob newMob;
                                UUID id = ent.getUniqueId();
                                if (spesificAbList.contains("1up")) {
                                    newMob = new InfernalMob(ent, id, true, spesificAbList, 2, getEffect());
                                } else {
                                    newMob = new InfernalMob(ent, id, true, spesificAbList, 1, getEffect());
                                }
                                if (spesificAbList.contains("flying")) {
                                    makeFly(ent);
                                }
                                infernalList.add(newMob);
                                gui.setName(ent);
                                giveMobGear(ent, false);

                                addHealth(ent, spesificAbList);

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
                            List<String> abList = new ArrayList(Arrays.asList(args).subList(6, args.length));
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
                            List<String> abList = new ArrayList(Arrays.asList(args).subList(3, args.length));
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
                            if (getTarget(player) != null) {
                                Entity targeted = getTarget(player);
                                UUID mobId = targeted.getUniqueId();
                                if (idSearch(mobId) != -1) {
                                    oldMobAbilityList = findMobAbilities(mobId);
                                    if (!targeted.isDead()) {
                                        sender.sendMessage("--Targeted InfernalMob's Abilities--");
                                        sender.sendMessage(oldMobAbilityList.toString());
                                    }
                                } else {
                                    sender.sendMessage("§cThis " + targeted.getType().getName() + " §cis not an infernal mob!");
                                }
                            } else {
                                sender.sendMessage("§cUnable to find mob!");
                            }
                        } else if ((args[0].equalsIgnoreCase("setInfernal")) && (args.length == 2)) {
                            if (player.getTargetBlock(null, 25).getType() == Material.SPAWNER) {
                                int delay = Integer.parseInt(args[1]);

                                String name = getLocationName(player.getTargetBlock(null, 25).getLocation());

                                mobSaveFile.set("infernalSpanwers." + name, delay);
                                mobSaveFile.save(saveYML);
                                sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                            } else {
                                sender.sendMessage("§cYou must be looking a spawner to make it infernal!");
                            }
                        } else if ((args[0].equalsIgnoreCase("kill")) && (args.length == 2)) {
                            int size = Integer.parseInt(args[1]);
                            for (Entity e : player.getNearbyEntities(size, size, size)) {
                                int id = idSearch(e.getUniqueId());
                                if (id != -1) {
                                    removeMob(id);
                                    e.remove();
                                    getLogger().log(Level.INFO, "Entity remove due to /kill");
                                }
                            }
                            sender.sendMessage("§eKilled all infernal mobs near you!");
                        } else if ((args[0].equalsIgnoreCase("killall")) && (args.length == 1 || args.length == 2)) {
                            World w = null;
                            if (args.length == 1 && sender instanceof Player) {
                                w = ((Player) sender).getWorld();
                            } else if (args.length == 2) {
                                w = getServer().getWorld(args[1]);
                            }

                            if (w != null) {
                                for (Entity e : w.getEntities()) {
                                    int id = idSearch(e.getUniqueId());
                                    if (id != -1) {
                                        removeMob(id);
                                        getLogger().log(Level.INFO, "Entity remove due to /killall");
                                        e.remove();
                                    }
                                }
                                sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                            } else {
                                sender.sendMessage("§cWorld not found!");
                            }
                        } else if (args[0].equalsIgnoreCase("mobs")) {
                            sender.sendMessage("§6List of Mobs:");
                            for (EntityType e : EntityType.values())
                                if (e != null)
                                    sender.sendMessage(e.toString());
                        } else if (args[0].equalsIgnoreCase("setloot")) {
                            setItem(player.getInventory().getItemInMainHand(), "loot." + args[1], lootFile);
                            sender.sendMessage("§eSet loot at index " + args[1] + " §eto item in hand.");
                        } else {
                            throwError(sender);
                        }
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                }
            } catch (Exception x) {
                throwError(sender);
                x.printStackTrace();
            }
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
    }
}