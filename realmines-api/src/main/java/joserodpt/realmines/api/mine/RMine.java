package joserodpt.realmines.api.mine;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.event.MineBlockBreakEvent;
import joserodpt.realmines.api.event.OnMineResetEvent;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.mine.components.actions.MineActionCommand;
import joserodpt.realmines.api.mine.components.actions.MineActionDropItem;
import joserodpt.realmines.api.mine.components.actions.MineActionGiveItem;
import joserodpt.realmines.api.mine.components.actions.MineActionMoney;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.components.items.MineSchematicItem;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.task.MineTimer;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.utils.ItemStackSpringer;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.api.utils.WorldEditUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RMine {

    public enum Type {BLOCKS, SCHEMATIC, FARM}

    public enum ResetCause {COMMAND, PLUGIN, TIMER, CREATION}

    public enum Reset {PERCENTAGE, TIME}

    public enum MineData {BLOCKS, ICON, TELEPORT, SIGNS, LOCATION, SETTINGS, NAME, FACES, COLOR, MINE_TYPE, ALL}

    protected String name;
    private World w;
    protected String displayName;
    protected List<MineSign> signs = new ArrayList<>();
    protected List<RMBlockSet> blockSets = new ArrayList<>();
    protected int blockSetIndex = 0;
    protected Location teleport;
    protected Material icon;
    protected boolean resetByPercentage, resetByTime, freezed, breakingPermissionOn, silent;
    protected int minedBlocks, resetByTimeValue, resetByPercentageValue;
    protected boolean highlight = false;
    protected HashMap<MineCuboid.CuboidDirection, Material> faces = new HashMap<>();

    protected MineTimer timer;
    protected MineColor color = MineColor.WHITE;
    protected MineCuboid mineCuboid;

    private File file;
    private FileConfiguration config;

    //create new mine
    public RMine(String name) throws RMFailedToLoadException {
        this.name = name;
        checkConfig(true, true);
    }

    //converting from old config to new config
    public RMine(String name, Section mineConfigSection) throws RMFailedToLoadException {
        this.name = name;

        //convert pre plugin version 1.8 config (from mines.yml)
        //used to load the mine config from the old format
        checkConfig(false, false);

        this.config.set("name", name);
        this.config.set("type", mineConfigSection.getString("Type"));
        this.config.set("world", mineConfigSection.getString("World"));
        this.config.set("icon", mineConfigSection.getString("Icon"));
        this.config.set("displayName", mineConfigSection.getString("Display-Name"));
        this.config.set("color", mineConfigSection.getString("Color"));

        if (getType() != Type.SCHEMATIC) {
            String pos1 = mineConfigSection.getString("POS1.X") + ";" + mineConfigSection.getString("POS1.Y") + ";" + mineConfigSection.getString("POS1.Z");
            config.set("pos1", pos1);
            String pos2 = mineConfigSection.getString("POS2.X") + ";" + mineConfigSection.getString("POS2.Y") + ";" + mineConfigSection.getString("POS2.Z");
            config.set("pos2", pos2);
        }

        this.config.set("reset.silent", mineConfigSection.getBoolean("Settings.Reset.Silent"));
        if (mineConfigSection.getStringList("Reset-Commands") != null) {
            this.config.set("reset.commands", mineConfigSection.getStringList("Reset-Commands"));
        } else {
            this.config.set("reset.commands", Collections.emptyList());
        }
        this.config.set("reset.percentage.active", mineConfigSection.getBoolean("Settings.Reset.ByPercentage"));
        this.config.set("reset.percentage.value", mineConfigSection.getInt("Settings.Reset.ByPercentageValue"));
        this.config.set("reset.time.active", mineConfigSection.getBoolean("Settings.Reset.ByTime"));
        this.config.set("reset.time.value", mineConfigSection.getInt("Settings.Reset.ByTimeValue"));

        this.config.set(RMineSettings.BREAK_PERMISSION.getConfigKey(), mineConfigSection.getBoolean("Settings.Break-Permission"));
        this.config.set(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES.getConfigKey(), mineConfigSection.getBoolean("Settings.Discard-Break-Action-Messages"));

        this.config.set("signs", mineConfigSection.getStringList("Signs"));

        if (mineConfigSection.getSection("Faces") != null) {
            mineConfigSection.getSection("Faces").getRoutesAsStrings(false).forEach(face -> {
                this.config.set("faces." + face, mineConfigSection.getString("Faces." + face));
            });
        }

        //round the values to avoid floating point errors to two decimal places
        float x = (float) Math.round(mineConfigSection.getDouble("Teleport.X") * 100) / 100;
        float y = (float) Math.round(mineConfigSection.getDouble("Teleport.Y") * 100) / 100;
        float z = (float) Math.round(mineConfigSection.getDouble("Teleport.Z") * 100) / 100;
        float yaw = (float) Math.round(mineConfigSection.getDouble("Teleport.Yaw") * 100) / 100;
        float pitch = (float) Math.round(mineConfigSection.getDouble("Teleport.Pitch") * 100) / 100;
        String teleport = x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
        this.config.set("teleport", teleport);


        //check if "Blocks" is a string list
        //convert old list of blocks to one new block-set
        if (mineConfigSection.getSection("Blocks") == null) { //directly convert the old block list to the new mine format
            /*
            since version 1.6, blocks have a new format
            convert old block format, like:
            - STONE;0.9
            - DIAMOND_ORE;0.1
            into:
            STONE:
              Chance: 0.9
              Break-Actions: ...
            DIAMOND_ORE:
              Chance: 0.1
              Break-Actions: ...
             */

            for (final String a : mineConfigSection.getStringList("Blocks")) {
                final String[] content = a.split(";");
                if (content.length != 2) {
                    RealMinesAPI.getInstance().getLogger().warning("Invalid pre version 1.6 block format for mine " + this.getName() + "! Skipping.");
                    continue;
                }
                this.config.set("block-sets.default." + content[0] + ".percentage", Double.parseDouble(content[1]));
            }
        } else {
            mineConfigSection.getSection("Blocks").getRoutesAsStrings(false).forEach(block -> {
                config.set("block-sets.default." + block + ".percentage", mineConfigSection.getSection("Blocks").getDouble(block + ".Chance"));

                //has any break actions?
                if (mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions") != null) {
                    //get a map of break actions
                    mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getRoutesAsStrings(false).forEach(action -> {
                        String type = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getString(action + ".Type");
                        double chanceBA = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getDouble(action + ".Chance");

                        config.set("block-sets.default." + block + ".break-actions." + action + ".type", type);
                        config.set("block-sets.default." + block + ".break-actions." + action + ".chance", chanceBA);

                        Section breakActions = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions");

                        for (String property : new String[]{"Command", "Amount", "Item"}) {
                            Object value = breakActions.get(action + "." + property);
                            if (value != null && !(value instanceof String && ((String) value).isEmpty())) {
                                config.set("block-sets.default." + block + ".break-actions." + action + ".value", value);
                                return;
                            }
                        }
                    });
                }
            });
        }

        saveConfig();

        //load the mine config from the new format
        RealMinesAPI.getInstance().getLogger().info("Loading now the new mine config for: " + this.getName());
        loadMineConfig();
    }

    public RMine(String name, YamlConfiguration mineConfig) throws RMFailedToLoadException {
        this.name = name;
        this.config = mineConfig;
        checkConfig(false, true);
    }

    private void loadMineConfig() throws RMFailedToLoadException {
        final String name = this.getName();

        this.displayName = this.config.getString("displayName");

        final String worldName = this.config.getString("world");
        if (worldName == null || worldName.isEmpty()) {
            throw new RMFailedToLoadException(name, "[RealMines] Could not load world " + worldName + ". Is the world name correct and valid? Skipping.");
        }

        final World w = Bukkit.getWorld(worldName);
        if (w == null) {
            throw new RMFailedToLoadException(name, "[RealMines] Could not load world " + worldName + ". Does the world exist and is loaded? Skipping.");
        }
        this.w = w;

        if (this.config.getString("icon") == null) {
            this.icon = Material.STONE;
            RealMinesAPI.getInstance().getLogger().warning("[RealMines] Could not load icon for mine " + name + ". Invalid material. Replacing with STONE.");
        } else {
            final Material icon = Material.getMaterial(this.config.getString("icon"));
            if (icon == null) {
                this.icon = Material.STONE;
                RealMinesAPI.getInstance().getLogger().warning("[RealMines] Could not load icon for mine " + name + ". Invalid material. Replacing with STONE.");
            } else {
                this.icon = icon;
            }
        }

        if (getType() != Type.SCHEMATIC) {
            final String[] pos1 = this.config.getString("pos1").split(";");
            if (pos1.length != 3) {
                throw new RMFailedToLoadException(name, "[RealMines] Could not load pos1 for mine " + name + ". Invalid length args for pos1. Skipping");
            }
            final String[] pos2 = this.config.getString("pos2").split(";");
            if (pos2.length != 3) {
                throw new RMFailedToLoadException(name, "[RealMines] Could not load pos2 for mine " + name + ". Invalid length args for pos2. Skipping");
            }
            final Location p1 = new Location(w, Double.parseDouble(pos1[0]), Double.parseDouble(pos1[1]), Double.parseDouble(pos1[2]));
            final Location p2 = new Location(w, Double.parseDouble(pos2[0]), Double.parseDouble(pos2[1]), Double.parseDouble(pos2[2]));
            this.mineCuboid = new MineCuboid(p1, p2);
        }

        if (this.config.get("teleport") != null) {
            final String[] teleport = this.config.getString("teleport").split(";");
            if (teleport.length != 5) {
                throw new RMFailedToLoadException(name, "[RealMines] Could not load teleport for mine " + name + ". Invalid length args for teleport. Skipping");
            }
            this.teleport = new Location(w, Double.parseDouble(teleport[0]), Double.parseDouble(teleport[1]), Double.parseDouble(teleport[2]), Float.parseFloat(teleport[3]), Float.parseFloat(teleport[4]));
        }

        if (this.config.get("signs") != null) {
            for (final String sig : this.config.getStringList("signs")) {
                final String[] parse = sig.split(";");
                if (parse.length != 5) {
                    throw new RMFailedToLoadException(name, "[RealMines] Could not load sign for mine " + name + ". Invalid length args for sign. Skipping");
                }
                final World sigw = Bukkit.getWorld(parse[0]);
                if (sigw == null) {
                    throw new RMFailedToLoadException(name, "[RealMines] Could not load sign for mine " + name + ". Invalid world name. Skipping");
                }
                final MineSign ms = new MineSign(sigw.getBlockAt(new Location(sigw, Double.parseDouble(parse[1]), Double.parseDouble(parse[2]),
                        Double.parseDouble(parse[3]))), parse[4]);
                this.signs.add(ms);
            }
        }

        if (this.config.get("faces") != null) {
            for (final String sig : this.config.getConfigurationSection("faces").getKeys(false)) {
                faces.put(MineCuboid.CuboidDirection.valueOf(sig), Material.valueOf(this.config.getString("faces." + sig)));
            }
        }

        if (this.config.getString("color") != null) {
            String value = this.config.getString("color");
            if (value != null && !value.isEmpty()) {
                this.color = MineColor.valueOf(value);
            }
        }

        //resets
        this.resetByPercentage = this.config.getBoolean("reset.percentage.active");
        this.resetByPercentageValue = this.config.getInt("reset.percentage.value");
        this.resetByTime = this.config.getBoolean("reset.time.active");
        this.resetByTimeValue = this.config.getInt("reset.time.value");

        //settings
        this.silent = this.config.getBoolean("reset.silent");
        this.breakingPermissionOn = this.config.getBoolean("settings.break-permission");

        //iterate over keys in the block-sets section
        for (String blockSetKey : this.config.getConfigurationSection("block-sets").getKeys(false)) {
            Map<Material, MineItem> items = new HashMap<>();

            for (final String mat : this.config.getConfigurationSection("block-sets." + blockSetKey).getKeys(false)) {
                final Double per = this.config.getDouble("block-sets." + blockSetKey + "." + mat + ".percentage");
                final Boolean disabledVanillaDrop = this.config.getBoolean("block-sets." + blockSetKey + "." + mat + ".disabled-vanilla-drop");
                final Boolean disabledBlockMining = this.config.getBoolean("block-sets." + blockSetKey + "." + mat + ".disabled-block-mining");

                try {
                    Material m = Material.valueOf(mat);

                    List<MineAction> actionsList = new ArrayList<>();

                    if (this.config.getConfigurationSection("block-sets." + blockSetKey + "." + mat + ".break-actions") != null) {
                        for (final String actionID : this.config.getConfigurationSection("block-sets." + blockSetKey + "." + mat + ".break-actions").getKeys(false)) {
                            final String actionRoute = "block-sets." + blockSetKey + "." + mat + ".break-actions." + actionID;
                            final Double chance = this.config.getDouble(actionRoute + ".chance");
                            try {
                                MineAction.Type mineactiontype = MineAction.Type.valueOf(this.config.getString(actionRoute + ".type"));
                                switch (mineactiontype) {
                                    case EXECUTE_COMMAND:
                                        actionsList.add(new MineActionCommand(actionID, name, chance, this.config.getString(actionRoute + ".value")));
                                        break;
                                    case DROP_ITEM:
                                        String data = this.config.getString(actionRoute + ".value");
                                        try {
                                            actionsList.add(new MineActionDropItem(actionID, name, chance, ItemStackSpringer.getItemDeSerializedJSON(data).clone()));
                                        } catch (Exception e) {
                                            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Badly formatted ItemStack: " + data);
                                            RealMinesAPI.getInstance().getPlugin().getLogger().warning("Item Serialized for " + mat + " isn't valid! Skipping.");
                                            continue;
                                        }
                                        break;
                                    case GIVE_ITEM:
                                        String data2 = this.config.getString(actionRoute + ".value");
                                        try {
                                            actionsList.add(new MineActionGiveItem(actionID, name, chance, ItemStackSpringer.getItemDeSerializedJSON(data2)));
                                        } catch (Exception e) {
                                            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Badly formatted ItemStack: " + data2);
                                            RealMinesAPI.getInstance().getPlugin().getLogger().warning("Item Serialized for " + mat + " isn't valid! Skipping.");
                                            continue;
                                        }
                                        break;
                                    case GIVE_MONEY:
                                        if (RealMinesAPI.getInstance().getEconomy() == null) {
                                            RealMinesAPI.getInstance().getPlugin().getLogger().warning("Money Break Action for " + mat + " will be ignored because Vault isn't installed on this server.");
                                            continue;
                                        }

                                        actionsList.add(new MineActionMoney(actionID, name, chance, this.config.getDouble(actionRoute + ".value")));
                                        break;
                                }
                            } catch (Exception e) {
                                RealMinesAPI.getInstance().getPlugin().getLogger().severe("Break Action Type " + this.config.getString(actionRoute + ".Type") + " is invalid! Skipping. This action is in mine: " + name);
                            }
                        }
                    }

                    switch (getType()) {
                        case BLOCKS:
                            items.put(m, new MineBlockItem(m, per, disabledVanillaDrop, disabledBlockMining, actionsList));
                            break;
                        case FARM:
                            items.put(m, new MineFarmItem(FarmItem.valueOf(mat), per, disabledVanillaDrop, disabledBlockMining, this.config.getInt("block-sets" + blockSetKey + "." + mat + ".age"), actionsList));
                            break;
                        case SCHEMATIC:
                            items.put(m, new MineSchematicItem(m, disabledVanillaDrop, disabledBlockMining, actionsList));
                            break;
                    }
                } catch (Exception e) {
                    RealMinesAPI.getInstance().getPlugin().getLogger().severe("Material type " + mat + " is invalid! Skipping. This material is in mine: " + getName());
                }
            }

            this.blockSets.add(new RMBlockSet(blockSetKey, items));
        }

        this.timer = new MineTimer(this);
        if (this.resetByTime) {
            this.timer.start();
        }
    }

    public FileConfiguration getMineConfig() {
        return this.config;
    }

    public boolean getBooleanSetting(RMineSettings rMineSettings) {
        return this.getMineConfig().getBoolean(rMineSettings.getConfigKey());
    }

    public void setBooleanSetting(RMineSettings rMineSettings, boolean b) {
        this.getMineConfig().set(rMineSettings.getConfigKey(), b);
        this.saveConfig();
    }

    private void checkConfig(boolean saveDefaultConfig, boolean loadConfig) throws RMFailedToLoadException {
        this.file = new File(RealMinesAPI.getInstance().getPlugin().getDataFolder() + "/mines/", this.getName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                if (saveDefaultConfig) {
                    setupDefaultConfig();
                }
            } catch (IOException e) {
                RealMinesAPI.getInstance().getLogger().severe("RealMinesAPI threw an error while creating config for " + this.getName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }

        if (loadConfig) loadMineConfig();
    }

    private void setupDefaultConfig() {

    }

    public void deleteConfig() {
        File fileToDelete = new File(RealMinesAPI.getInstance().getPlugin().getDataFolder() + "/mines/", this.getName() + ".yml");

        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                RealMinesAPI.getInstance().getLogger().severe("Failed to delete Configuration file for " + this.getName() + ".");
            }
        } else {
            RealMinesAPI.getInstance().getLogger().severe("Configuration file for " + this.getName() + " doesn't exist.");
        }
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            RealMinesAPI.getInstance().getLogger().severe("RealMinesAPI threw an error while saving config for " + this.getName());
        }
    }

    public MineColor getMineColor() {
        return this.color;
    }

    public void setMineColor(MineColor color) {
        this.color = color;
    }

    public boolean hasFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up) != null;
    }

    public MineTimer getMineTimer() {
        return this.timer;
    }

    public Location getPOS1() {
        return this.getMineCuboid().getPOS1();
    }

    public Location getPOS2() {
        return this.getMineCuboid().getPOS2();
    }

    public void setPOS(final Location p1, final Location p2) {
        this.mineCuboid = new MineCuboid(p1, p2);
    }

    public boolean hasTP() {
        return this.teleport != null;
    }

    public List<String> getSignList() {
        return this.getSigns().stream().map(mineSign -> mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()).collect(Collectors.toList());
    }

    public String getBar() {
        return Text.getProgressBar(this.getRemainingBlocks(), this.getBlockCount(), 10, '■', ChatColor.GREEN, ChatColor.RED);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String input) {
        this.displayName = input;
        this.saveData(MineData.NAME);
    }

    public MineCuboid getMineCuboid() {
        return this.mineCuboid;
    }

    //block counts
    public int getBlockCount() {
        return this.getMineCuboid() == null ? 0 : this.getMineCuboid().getTotalBlocks();
    }

    public int getMinedBlocks() {
        return this.minedBlocks;
    }

    public int getRemainingBlocks() {
        return this.getBlockCount() - this.getMinedBlocks();
    }
    //block counts

    //block percentages
    public int getRemainingBlocksPer() {
        return this.getBlockCount() == 0 ? 0 : (this.getRemainingBlocks() * 100 / this.getBlockCount());
    }

    public int getMinedBlocksPer() {
        return this.getBlockCount() == 0 ? 0 : (this.getMinedBlocks() * 100 / this.getBlockCount());
    }
    //block percentages

    public abstract void fillContent();

    public void fillFaces() {
        if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
            for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                MineCuboid face = this.getMineCuboid().getFace(pair.getKey());
                BlockVector3 p1 = BlockVector3.at(face.getMin().getX(), face.getMin().getY(), face.getMin().getZ());
                BlockVector3 p2 = BlockVector3.at(face.getMax().getX(), face.getMax().getY(), face.getMax().getZ());

                RandomPattern solid = new RandomPattern();
                solid.add(BukkitAdapter.adapt(pair.getValue().createBlockData()).toBaseBlock(), 100);

                WorldEditUtils.setBlocks(new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), p1, p2), solid);
            }
        } else {
            for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                this.getMineCuboid().getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
            }
        }
    }

    public Map<Material, MineItem> getMineItems() {
        ++this.blockSetIndex;
        if (this.blockSetIndex >= this.blockSets.size()) {
            this.blockSetIndex = 0;
        }

        return this.blockSets.get(this.blockSetIndex).getItems();
    }

    public List<MineItem> getBlockIcons() {
        return this.getMineItems().isEmpty() ? new ArrayList<>(Collections.singletonList(new MineItem())) :
                new ArrayList<>(this.getMineItems().values());
    }

    public void saveData(final MineData t) {
        this.save(t);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    private void save(MineData t) {
        switch (t) {
            case ICON:
                this.config.set("icon", this.icon.name());
                break;
            case TELEPORT:
                this.config.set("teleport", this.teleport);
                break;
            case SIGNS:
                this.config.set("signs", this.getSignList());
                break;
            case LOCATION:
                this.config.set("pos1", this.getPOS1());
                this.config.set("pos2", this.getPOS2());
                break;
            case SETTINGS:
                //
                break;
            case NAME:
                this.config.set("displayName", this.displayName);
                break;
            case FACES:
                this.config.set("faces", this.getFaces().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                break;
            case COLOR:
                this.config.set("color", this.getMineColor().name());
                break;
            case MINE_TYPE:
                this.config.set("type", this.getType().name());
                break;
            case ALL:
                this.save(MineData.ICON);
                this.save(MineData.TELEPORT);
                this.save(MineData.SIGNS);
                this.save(MineData.LOCATION);
                this.save(MineData.SETTINGS);
                this.save(MineData.NAME);
                this.save(MineData.FACES);
                this.save(MineData.COLOR);
                this.save(MineData.MINE_TYPE);
                break;
        }
        saveConfig();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void saveAll() {
        this.save(MineData.ALL);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    public void reset() {
        reset(ResetCause.PLUGIN);
    }

    public void reset(ResetCause re) {
        if (!Bukkit.getOnlinePlayers().isEmpty() || RMConfig.file().getBoolean("RealMines.resetMinesWhenNoPlayers")) {

            OnMineResetEvent event = new OnMineResetEvent(this, re);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            this.kickPlayers(TranslatableLine.MINE_RESET_STARTING.setV1(TranslatableLine.ReplacableVar.MINE.eq(this.getDisplayName())).get());
            this.fillContent();

            //reset mined blocks
            this.minedBlocks = 0;
            processBlockBreakEvent(false);

            //execute reset commands
            this.config.getStringList("reset.commands").forEach(s -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s));

            if (!this.isSilent()) {
                if (RMConfig.file().getBoolean("RealMines.broadcastResetMessageOnlyInWorld")) {
                    this.getMineCuboid().getWorld().getPlayers().forEach(player -> TranslatableLine.MINE_RESET_ANNOUNCEMENT.setV1(TranslatableLine.ReplacableVar.MINE.eq(this.getDisplayName())).send(player));
                } else {
                    Bukkit.broadcastMessage(Text.getPrefix() + TranslatableLine.MINE_RESET_ANNOUNCEMENT.setV1(TranslatableLine.ReplacableVar.MINE.eq(this.getDisplayName())).get());
                }
            }
        }
    }

    public void addSign(final Block block, final String modif) {
        this.signs.add(new MineSign(block, modif));
        this.saveData(MineData.SIGNS);
    }

    @SuppressWarnings("deprecation")
    public void updateSigns() {
        Bukkit.getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), () -> {
            for (final MineSign ms : this.signs) {
                if (ms.getBlock().getType().name().contains("SIGN")) {
                    final Sign sign = (Sign) ms.getBlock().getState();
                    final String modif = ms.getModifier();

                    switch (modif.toLowerCase()) {
                        case "pm":
                            sign.setLine(1, this.getMinedBlocksPer() + "%");
                            sign.setLine(2, TranslatableLine.SIGNS_MINED_ON.get());
                            break;
                        case "bm":
                            sign.setLine(1, String.valueOf(this.getMinedBlocks()));
                            sign.setLine(2, TranslatableLine.SIGNS_MINED_BLOCKS_ON.get());
                            break;
                        case "br":
                            sign.setLine(1, String.valueOf(this.getRemainingBlocks()));
                            sign.setLine(2, TranslatableLine.SIGNS_BLOCKS_ON.get());
                            break;
                        case "pl":
                            sign.setLine(1, this.getRemainingBlocksPer() + "%");
                            sign.setLine(2, TranslatableLine.SIGNS_LEFT_ON.get());
                            break;
                    }

                    sign.setLine(0, Text.getPrefix());
                    sign.setLine(3, Text.color(this.getDisplayName()));
                    sign.update();
                }
            }
        });
    }

    public void clear() {
        this.minedBlocks = 0;
        processBlockBreakEvent(false);

        this.clearContents();
    }

    public boolean isSilent() {
        return this.silent;
    }

    public void kickPlayers(final String s) {
        if (this.getType() != Type.FARM) {
            if (RMConfig.file().getBoolean("RealMines.teleportPlayers")) {
                this.getPlayersInMine().forEach(player -> RealMinesAPI.getInstance().getMineManager().teleport(player, this, this.isSilent(), false));
            }
        }
        if (!this.isSilent()) {
            this.broadcastMessage(s);
        }
    }

    public void broadcastMessage(String s) {
        this.getPlayersInMine().forEach(p -> Text.send(p, s));
        if (RMConfig.file().getBoolean("RealMines.actionbarMessages"))
            this.getPlayersInMine().forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Text.color(s))));
    }

    public List<Player> getPlayersInMine() {
        if (this.mineCuboid == null) {
            return Collections.emptyList();
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> this.mineCuboid.contains(p.getLocation()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeDependencies() {
        this.signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
    }

    public List<Location> getHighlightedCube() {
        final List<Location> result = new ArrayList<>();
        final World world = this.mineCuboid.getPOS1().getWorld();
        final double minX = Math.min(this.mineCuboid.getPOS1().getX(), this.mineCuboid.getPOS2().getX());
        final double minY = Math.min(this.mineCuboid.getPOS1().getY(), this.mineCuboid.getPOS2().getY());
        final double minZ = Math.min(this.mineCuboid.getPOS1().getZ(), this.mineCuboid.getPOS2().getZ());
        final double maxX = Math.max(this.mineCuboid.getPOS1().getX() + 1, this.mineCuboid.getPOS2().getX() + 1);
        final double maxY = Math.max(this.mineCuboid.getPOS1().getY() + 1, this.mineCuboid.getPOS2().getY() + 1);
        final double maxZ = Math.max(this.mineCuboid.getPOS1().getZ() + 1, this.mineCuboid.getPOS2().getZ() + 1);
        final double dist = 0.5D;
        for (double x = minX; x <= maxX; x += dist) {
            for (double y = minY; y <= maxY; y += dist) {
                for (double z = minZ; z <= maxZ; z += dist) {
                    int components = 0;
                    if (x == minX || x == maxX) ++components;
                    if (y == minY || y == maxY) ++components;
                    if (z == minZ || z == maxZ) ++components;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        return result;
    }

    public World getWorld() {
        return this.w;
    }

    public void highlight() {
        if (this.highlight) {
            this.getHighlightedCube().forEach(location -> location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(this.getMineColor().getColor(), 1)));
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isResetBy(final Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentage;
            case TIME:
                return this.resetByTime;
        }
        return false;
    }

    public int getResetValue(final Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentageValue;
            case TIME:
                return this.resetByTimeValue;
        }
        return -1;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setReset(final Reset e, final boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
        }
    }

    public void setReset(final Reset e, final int d) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentageValue = d;
                break;
            case TIME:
                this.resetByTimeValue = d;
                break;
        }
    }

    public Material getIcon() {
        return this.icon;
    }

    public ItemStack getMineIcon() {
        return Items.createItem(this.getIcon(), 1, this.getMineColor().getColorPrefix() + " &f&l" + this.getDisplayName() + " &7[&b&l" + this.getType().name() + "&r&7]", RMLanguageConfig.file().getStringList("GUI.Items.Mine.Description")
                .stream()
                .map(s -> Text.color(s
                        .replaceAll("%remainingblocks%", String.valueOf(this.getRemainingBlocks()))
                        .replaceAll("%totalblocks%", String.valueOf(this.getBlockCount()))
                        .replaceAll("%bar%", this.getBar())))
                .collect(Collectors.toList()));
    }

    public void setIcon(final Material a) {
        this.icon = a;
        this.saveData(MineData.ICON);
    }

    public boolean isHighlighted() {
        return this.highlight;
    }

    public void setHighlight(final boolean b) {
        this.highlight = b;
    }

    public Location getTeleport() {
        return this.teleport;
    }

    public void setTeleport(final Location location) {
        this.teleport = location;
    }

    public List<MineSign> getSigns() {
        return this.signs;
    }

    public MineTimer getTimer() {
        return this.timer;
    }

    public Material getFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up);
    }

    public void setFaceBlock(final MineCuboid.CuboidDirection cd, final Material a) {
        this.faces.put(cd, a);
        this.saveData(MineData.FACES);
    }

    public void removeFaceblock(final MineCuboid.CuboidDirection d) {
        this.faces.remove(d);
        this.saveData(MineData.FACES);
    }

    public HashMap<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    public abstract RMine.Type getType();

    public void processBlockBreakAction(final MineBlockBreakEvent e, final Double random) {
        if (e.isBroken() && this.getMineItems().containsKey(e.getMaterial())) {
            this.getMineItems().get(e.getMaterial()).getBreakActions().forEach(mineAction -> mineAction.execute(e.getPlayer(), e.getBlock().getLocation(), random));
        }
    }

    public void processBlockBreakEvent(final MineBlockBreakEvent event, final boolean reset) {
        //add or remove to mined blocks
        this.minedBlocks = Math.max(0, this.minedBlocks + (event.isBroken() ? 1 : -1));

        if (event.getPlayer() != null) {
            processBlockBreakAction(event, RealMinesAPI.getRand().nextDouble() * 100);
        }

        processBlockBreakEvent(reset);
    }

    private void processBlockBreakEvent(boolean reset) {
        if (reset) {
            //if mine reset percentage is lower, reset it
            if (this.isResetBy(RMine.Reset.PERCENTAGE) & ((double) this.getRemainingBlocksPer() < this.getResetValue(RMine.Reset.PERCENTAGE))) {
                this.kickPlayers(TranslatableLine.MINE_RESET_PERCENTAGE.get());
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), this::reset, 10);
            }
        }

        //update min e signs
        this.updateSigns();
    }

    public abstract void clearContents();

    public boolean isFreezed() {
        return this.freezed;
    }

    public void setFreezed(boolean freezed) {
        this.freezed = freezed;
    }

    public void setBreakingPermissionOn(boolean breakingPermissionOn) {
        this.breakingPermissionOn = breakingPermissionOn;
    }

    public boolean isBreakingPermissionOn() {
        return this.breakingPermissionOn;
    }

    public String getBreakPermission() {
        return "realmines." + this.getName() + ".break";
    }
}
