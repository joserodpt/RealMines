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
import joserodpt.realmines.api.mine.components.RMBlockSet;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.RMineSettings;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class RMine {

    public void setBlockSetMode(BlockSetsMode next) {
        this.blockSetsMode = next;
        setSettingString(RMineSettings.BLOCK_SETS_MODE, this.getBlockSetMode().name());
    }

    public enum Type {BLOCKS, SCHEMATIC, FARM}

    public enum ResetCause {COMMAND, PLUGIN, TIMER, CREATION, IMPORT}

    public enum Reset {PERCENTAGE, TIME}

    public enum BlockSetsMode {
        INCREMENTAL("&aIncremental"),
        RANDOM("&eRandom"),
        NONE("&fNone");

        String displayName;

        BlockSetsMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public BlockSetsMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum MineData {BLOCKS, ICON, RESET, TELEPORT, SIGNS, POS, NAME, DISPLAYNAME, FACES, COLOR, MINE_TYPE, ALL}

    protected String name, displayName;

    private World w;
    protected Location teleport;
    protected Material icon;
    protected List<MineSign> signs = new ArrayList<>();
    protected Map<String, RMBlockSet> blockSets = new HashMap<>();
    protected BlockSetsMode blockSetsMode = BlockSetsMode.INCREMENTAL;

    protected boolean freezed, silent;
    protected boolean resetByTime = true, resetByPercentage = true;

    protected int resetByTimeValue = 120, resetByPercentageValue = 20;
    protected int minedBlocks, blockSetIndex;

    protected boolean highlight = false;
    protected Map<MineCuboid.CuboidDirection, Material> faces = new HashMap<>();

    protected MineTimer timer;
    protected MineColor color = MineColor.WHITE;
    protected MineCuboid mineCuboid;
    protected Location _pos1, _pos2;
    private File file;

    private FileConfiguration config;

    //create new mine (for mines without pos1/2)
    public RMine(String name, World w) throws RMFailedToLoadException {
        this.name = ChatColor.stripColor(Text.color(name));
        this.displayName = name;
        this.w = w;

        switch (getType()) {
            case BLOCKS:
                this.icon = Material.DIAMOND_ORE;
                this.color = MineColor.BLUE;
                break;
            case SCHEMATIC:
                this.icon = Material.FILLED_MAP;
                this.color = MineColor.ORANGE;
                break;
            case FARM:
                this.icon = Material.WHEAT;
                this.color = MineColor.GREEN;
                break;
        }

        checkConfig(true, false);

        this.fillContent();
        this.updateSigns();
    }

    //create new mine
    public RMine(String name, World w, Location pos1, Location pos2) throws RMFailedToLoadException {
        this.name = ChatColor.stripColor(Text.color(name));
        this.displayName = name;
        this.w = w;
        this._pos1 = pos1;
        this._pos2 = pos2;
        this.mineCuboid = new MineCuboid(pos1, pos2);

        switch (getType()) {
            case BLOCKS:
                this.icon = Material.DIAMOND_ORE;
                this.color = MineColor.BLUE;
                break;
            case SCHEMATIC:
                this.icon = Material.FILLED_MAP;
                this.color = MineColor.ORANGE;
                break;
            case FARM:
                this.icon = Material.WHEAT;
                this.color = MineColor.GREEN;
                break;
        }

        checkConfig(true, false);

        this.fillContent();
        this.updateSigns();
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
        } else {
            this.config.set("schematic", mineConfigSection.getString("Schematic-Filename"));

            //round the values to avoid floating point errors to 0 decimal places
            float x = (float) Math.round(mineConfigSection.getDouble("Place.X") * 100) / 100;
            float y = (float) Math.round(mineConfigSection.getDouble("Place.Y") * 100) / 100;
            float z = (float) Math.round(mineConfigSection.getDouble("Place.Z") * 100) / 100;
            config.set("pos1", x + ";" + y + ";" + z);
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
        this.config.set(RMineSettings.BLOCK_SETS_MODE.getConfigKey(), this.getBlockSetMode().name());

        this.config.set("signs", mineConfigSection.getStringList("Signs"));

        if (mineConfigSection.getSection("Faces") != null) {
            mineConfigSection.getSection("Faces").getRoutesAsStrings(false).forEach(face -> this.config.set("faces." + face, mineConfigSection.getString("Faces." + face)));
        }

        //round the values to avoid floating point errors to two decimal places
        float x = (float) Math.round(mineConfigSection.getDouble("Teleport.X") * 100) / 100;
        float y = (float) Math.round(mineConfigSection.getDouble("Teleport.Y") * 100) / 100;
        float z = (float) Math.round(mineConfigSection.getDouble("Teleport.Z") * 100) / 100;
        float yaw = (float) Math.round(mineConfigSection.getDouble("Teleport.Yaw") * 100) / 100;
        float pitch = (float) Math.round(mineConfigSection.getDouble("Teleport.Pitch") * 100) / 100;
        String teleport = x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
        this.config.set("teleport", teleport);

        this.config.set("block-sets.default.description", "Default description for block set");
        this.config.set("block-sets.default.icon", Material.CAULDRON.name());

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
                this.config.set("block-sets.default.blocks." + content[0] + ".percentage", Double.parseDouble(content[1]));
            }
        } else {
            mineConfigSection.getSection("Blocks").getRoutesAsStrings(false).forEach(block -> {
                config.set("block-sets.default.blocks." + block + ".percentage", mineConfigSection.getSection("Blocks").getDouble(block + ".Chance"));

                config.set("block-sets.default.blocks." + block + ".disabled-vanilla-drop", mineConfigSection.getSection("Blocks").getBoolean(block + ".Disabled-Vanilla-Drop", false));
                config.set("block-sets.default.blocks." + block + ".disabled-block-mining", mineConfigSection.getSection("Blocks").getBoolean(block + ".Disabled-Block-Mining", false));

                //check if it has an age entry
                if (mineConfigSection.getSection("Blocks").getSection(block).get("Age") != null) {
                    config.set("block-sets.default.blocks." + block + ".age", mineConfigSection.getSection("Blocks").getInt(block + ".Age"));
                }

                //has any break actions?
                if (mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions") != null) {
                    //get a map of break actions
                    mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getRoutesAsStrings(false).forEach(action -> {
                        String type = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getString(action + ".Type");
                        double chanceBA = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions").getDouble(action + ".Chance");

                        config.set("block-sets.default.blocks." + block + ".break-actions." + action + ".type", type);
                        config.set("block-sets.default.blocks." + block + ".break-actions." + action + ".chance", chanceBA);

                        Section breakActions = mineConfigSection.getSection("Blocks").getSection(block).getSection("Break-Actions");

                        for (String property : new String[]{"Command", "Amount", "Item"}) {
                            Object value = breakActions.get(action + "." + property);
                            if (value != null && !(value instanceof String && ((String) value).isEmpty())) {
                                config.set("block-sets.default.blocks." + block + ".break-actions." + action + ".value", value);
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

    public FileConfiguration getMineConfig() {
        return this.config;
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
            setPOS(p1, p2);
        } else {
            final String posSTR = this.config.getString("pos1");
            if (posSTR == null) {
                throw new RMFailedToLoadException(name, "[RealMines] Could not load pos1 for mine " + name + ". Invalid pos1. Skipping");
            }

            final String[] pos1 = posSTR.split(";");
            if (pos1.length != 3) {
                throw new RMFailedToLoadException(name, "[RealMines] Could not load pos1 for mine " + name + ". Invalid length args for pos1. Skipping");
            }
            final Location p1 = new Location(w, Double.parseDouble(pos1[0]), Double.parseDouble(pos1[1]), Double.parseDouble(pos1[2]));
            this._pos1 = p1;
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
        this.silent = this.config.getBoolean("reset.silent");

        this.blockSetsMode = BlockSetsMode.valueOf(getSettingString(RMineSettings.BLOCK_SETS_MODE));

        //iterate over keys in the block-sets section

        if (this.config.getConfigurationSection("block-sets") != null) {
            for (String blockSetKey : this.config.getConfigurationSection("block-sets").getKeys(false)) {
                Map<Material, MineItem> items = new HashMap<>();

                if (this.config.getConfigurationSection("block-sets." + blockSetKey + ".blocks") != null) {
                    for (final String mat : this.config.getConfigurationSection("block-sets." + blockSetKey + ".blocks").getKeys(false)) {
                        final Double per = this.config.getDouble("block-sets." + blockSetKey + ".blocks." + mat + ".percentage");
                        final Boolean disabledVanillaDrop = this.config.getBoolean("block-sets." + blockSetKey + ".blocks." + mat + ".disabled-vanilla-drop");
                        final Boolean disabledBlockMining = this.config.getBoolean("block-sets." + blockSetKey + ".blocks." + mat + ".disabled-block-mining");

                        try {
                            Material m = Material.valueOf(mat);

                            List<MineAction> actionsList = new ArrayList<>();

                            if (this.config.getConfigurationSection("block-sets." + blockSetKey + ".blocks." + mat + ".break-actions") != null) {
                                for (final String actionID : this.config.getConfigurationSection("block-sets." + blockSetKey + ".blocks." + mat + ".break-actions").getKeys(false)) {
                                    final String actionRoute = "block-sets." + blockSetKey + ".blocks." + mat + ".break-actions." + actionID;
                                    final Double chance = this.config.getDouble(actionRoute + ".chance");
                                    try {
                                        MineAction.MineActionType mineactiontype = MineAction.MineActionType.valueOf(this.config.getString(actionRoute + ".type"));
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
                                    items.put(m, new MineFarmItem(FarmItem.valueOf(mat), per, disabledVanillaDrop, disabledBlockMining, this.config.getInt("block-sets." + blockSetKey + ".blocks." + mat + ".age", 0), actionsList));
                                    break;
                                case SCHEMATIC:
                                    items.put(m, new MineSchematicItem(m, disabledVanillaDrop, disabledBlockMining, actionsList));
                                    break;
                            }
                        } catch (Exception e) {
                            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Material type " + mat + " is invalid! Skipping. This material is in mine: " + getName());
                        }
                    }
                }

                Material icon = Material.BARRIER;
                try {
                    icon = Material.getMaterial(Objects.requireNonNull(this.config.getString("block-sets." + blockSetKey + ".icon")));
                } catch (Exception e) {
                    RealMinesAPI.getInstance().getPlugin().getLogger().severe("Icon for block set " + blockSetKey + " is invalid! Skipping.");
                }

                this.blockSets.put(blockSetKey, new RMBlockSet(blockSetKey, this.config.getString("block-sets." + blockSetKey + ".description"), icon, items));
            }
        }

        this.timer = new MineTimer(this);
        if (this.resetByTime) {
            this.timer.start();
        }
    }

    public String getSettingString(RMineSettings rMineSettings) {
        return this.getMineConfig().getString(rMineSettings.getConfigKey());
    }

    public void setSettingString(RMineSettings rMineSettings, String s) {
        this.getMineConfig().set(rMineSettings.getConfigKey(), s);
        this.saveConfig();
    }

    public boolean getSettingBool(RMineSettings rMineSettings) {
        return this.getMineConfig().getBoolean(rMineSettings.getConfigKey());
    }

    public void setSettingBool(RMineSettings rMineSettings, boolean b) {
        this.getMineConfig().set(rMineSettings.getConfigKey(), b);
        this.saveConfig();
    }

    private void setConfigFile(boolean saveDefaultConfig) {
        this.file = new File(RealMinesAPI.getInstance().getPlugin().getDataFolder() + "/mines/", this.getName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                if (saveDefaultConfig) {
                    if (this.config == null) {
                        this.config = YamlConfiguration.loadConfiguration(file);
                    }
                    saveDefaultConfig();
                    return;
                }
            } catch (IOException e) {
                RealMinesAPI.getInstance().getLogger().severe("RealMinesAPI threw an error while creating config for " + this.getName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
    }

    private void checkConfig(boolean saveDefaultConfig, boolean loadConfig) throws RMFailedToLoadException {
        setConfigFile(saveDefaultConfig);

        if (loadConfig) loadMineConfig();
    }

    public void rename(String s) {
        this.name = s;
        this.displayName = s;

        setConfigFile(false);

        this.saveData(MineData.NAME);
        this.saveData(MineData.POS);
    }

    private void saveDefaultConfig() {
        this.config.set("name", getName());
        this.config.set("type", getType().name());
        this.config.set("world", getWorld().getName());
        this.config.set("icon", getIcon().name());
        this.config.set("displayName", getDisplayName());
        this.config.set("color", getMineColor().name());

        if (getType() != Type.SCHEMATIC) {
            String pos1 = getPOS1().getX() + ";" + getPOS1().getY() + ";" + getPOS1().getZ();
            this.config.set("pos1", pos1);
            String pos2 = getPOS2().getX() + ";" + getPOS2().getY() + ";" + getPOS2().getZ();
            this.config.set("pos2", pos2);
        }

        this.config.set("reset.silent", isSilent());
        this.config.set("reset.commands", Collections.emptyList());
        this.config.set("reset.percentage.active", isResetBy(Reset.PERCENTAGE));
        this.config.set("reset.percentage.value", getResetValue(Reset.PERCENTAGE));
        this.config.set("reset.time.active", isResetBy(Reset.TIME));
        this.config.set("reset.time.value", getResetValue(Reset.TIME));

        this.config.set(RMineSettings.BREAK_PERMISSION.getConfigKey(), false);
        this.config.set(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES.getConfigKey(), false);
        this.config.set(RMineSettings.BLOCK_SETS_MODE.getConfigKey(), this.getBlockSetMode().name());

        this.config.set("signs", Collections.emptyList());
        this.config.set("block-sets", Collections.emptyList());

        saveConfig();
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

    public BlockSetsMode getBlockSetMode() {
        return this.blockSetsMode;
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
        saveData(MineData.COLOR);
    }

    public boolean hasFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up) != null;
    }

    public MineTimer getMineTimer() {
        return this.timer;
    }

    public Location getPOS1() {
        return _pos1;
    }

    public Location getPOS2() {
        return _pos2;
    }

    public void setPOS(final Location p1, final Location p2) {
        if (p2 == null) {
            //it's a setPOS for a schematic mine
            this._pos1 = p1;
            this._pos2 = p1;
        } else {
            if (getType() != Type.SCHEMATIC) {
                this._pos1 = p1;
                this._pos2 = p2;
            }
            this.mineCuboid = new MineCuboid(p1, p2);
        }

        saveData(MineData.POS);
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
        this.saveData(MineData.DISPLAYNAME);
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
        return this.blockSets.values().stream()
                .skip(this.blockSetIndex)
                .findFirst()
                .orElse(new RMBlockSet())
                .getItems();
    }

    public String getCurrentBlockSet() {
        return this.blockSets.values().stream()
                .skip(this.blockSetIndex)
                .findFirst()
                .map(RMBlockSet::getKey)
                .orElse("default");
    }

    public List<MineItem> getBlockIcons(String blockSet) {
        return this.getMineItemsOfSet(blockSet).isEmpty() ? new ArrayList<>(Collections.singletonList(new MineItem())) :
                new ArrayList<>(this.getMineItemsOfSet(blockSet).values());
    }

    public Map<Material, MineItem> getMineItemsOfSet(String key) {
        if (key == null || key.isEmpty()) {
            return getMineItems();
        }

        return this.getBlockSets().stream().filter(blockSet -> blockSet.getKey().equals(key)).findFirst().map(RMBlockSet::getItems).orElse(new HashMap<>());
    }

    public void removeBlockSet(RMBlockSet blockSet) {
        this.blockSets.remove(blockSet.getKey());
        this.saveData(MineData.BLOCKS);
    }

    public RMBlockSet addBlockSet(String name) {
        RMBlockSet s;
        if (name == null) {
            s = new RMBlockSet();
        } else {
            s = new RMBlockSet(name);
        }
        this.blockSets.put(s.getKey(), s);
        this.saveData(MineData.BLOCKS);
        return s;
    }

    protected RMBlockSet getBlockSet(String blockSetKey) {
        if (blockSetKey.equalsIgnoreCase("default") && !this.blockSets.containsKey("default")) {
            RMBlockSet s = new RMBlockSet("default");
            this.blockSets.put(s.getKey(), s);
            this.saveData(MineData.BLOCKS);
            return s;
        }

        return this.blockSets.get(blockSetKey);
    }

    public void renameBlockSet(String oldKey, String newKey) {
        RMBlockSet blockSet = this.blockSets.get(oldKey);
        this.blockSets.remove(oldKey);
        blockSet.setKey(newKey);
        this.blockSets.put(newKey, blockSet);
        this.saveData(MineData.BLOCKS);
    }

    public void processBlockBreakAction(final MineBlockBreakEvent e, final Double random) {
        if (e.isBroken() && this.getMineItems().containsKey(e.getMaterial())) {
            MineItem item = this.getMineItems().get(e.getMaterial());
            if (item != null) {
                item.getBreakActions().forEach(mineAction -> mineAction.execute(e.getPlayer(), e.getBlock().getLocation(), random));
            }
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

    public void saveData(final MineData t) {
        this._save(t, true);
        if (this.getTimer() != null) {
            if (!this.resetByTime) {
                this.timer.kill();
            } else {
                this.timer.restart();
            }
        }
    }

    private void _save(MineData t, boolean save) {
        switch (t) {
            case ICON:
                this.config.set("icon", this.getIcon().name());
                break;
            case TELEPORT:
                String teleport = this.teleport.getBlockX() + ";" + this.teleport.getBlockY() + ";" + this.teleport.getBlockZ() + ";" + this.teleport.getYaw() + ";" + this.teleport.getPitch();
                this.config.set("teleport", teleport);
                break;
            case RESET:
                this.config.set("reset.silent", isSilent());
                this.config.set("reset.percentage.active", isResetBy(Reset.PERCENTAGE));
                this.config.set("reset.percentage.value", getResetValue(Reset.PERCENTAGE));
                this.config.set("reset.time.active", isResetBy(Reset.TIME));
                this.config.set("reset.time.value", getResetValue(Reset.TIME));
                break;
            case SIGNS:
                this.config.set("signs", this.getSignList());
                break;
            case POS:
                String pos1 = getPOS1().getBlockX() + ";" + getPOS1().getBlockY() + ";" + getPOS1().getBlockZ();
                config.set("pos1", pos1);

                if (getType() != Type.SCHEMATIC) {
                    String pos2 = getPOS2().getBlockX() + ";" + getPOS2().getBlockY() + ";" + getPOS2().getBlockZ();
                    config.set("pos2", pos2);
                }
                break;
            case NAME:
                this.config.set("name", this.getName());
                break;
            case DISPLAYNAME:
                this.config.set("displayName", this.getDisplayName());
            case FACES:
                this.config.set("faces", this.getFaces().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                break;
            case COLOR:
                this.config.set("color", this.getMineColor().name());
                break;
            case MINE_TYPE:
                this.config.set("type", this.getType().name());
                break;
            case BLOCKS:
                config.set("block-sets", Collections.emptyList());
                this.getBlockSets().forEach(blockSetObject -> {
                    String blockSetKey = blockSetObject.getKey();

                    config.set("block-sets." + blockSetKey + ".description", blockSetObject.getDescription());
                    config.set("block-sets." + blockSetKey + ".icon", blockSetObject.getIconMaterial().name());

                    if (blockSetObject.getItems().isEmpty()) {
                        config.set("block-sets." + blockSetKey + ".blocks", Collections.emptyList());
                    } else {
                        blockSetObject.getItems().forEach(((material, mineItem) ->
                        {
                            String block = material.name();

                            config.set("block-sets." + blockSetKey + ".blocks." + block + ".percentage", mineItem.getPercentage());
                            config.set("block-sets." + blockSetKey + ".blocks." + block + ".disabled-vanilla-drop", mineItem.areVanillaDropsDisabled());
                            config.set("block-sets." + blockSetKey + ".blocks." + block + ".disabled-block-mining", mineItem.isBlockMiningDisabled());

                            if (mineItem instanceof MineFarmItem) {
                                config.set("block-sets." + blockSetKey + ".blocks." + block + ".age", ((MineFarmItem) mineItem).getAge());
                            }

                            if (mineItem.hasBreakActions()) {
                                mineItem.getBreakActions().forEach(action -> {
                                    config.set("block-sets." + blockSetKey + ".blocks." + block + ".break-actions." + action.getID() + ".type", action.getType().name());
                                    config.set("block-sets." + blockSetKey + ".blocks." + block + ".break-actions." + action.getID() + ".chance", action.getChance());
                                    config.set("block-sets." + blockSetKey + ".blocks." + block + ".break-actions." + action.getID() + ".value", action.getValue());
                                });
                            }
                        }));
                    }
                });
                break;
            case ALL:
                this._save(MineData.ICON, false);
                this._save(MineData.TELEPORT, false);
                this._save(MineData.SIGNS, false);
                this._save(MineData.POS, false);
                this._save(MineData.NAME, false);
                this._save(MineData.FACES, false);
                this._save(MineData.COLOR, false);
                this._save(MineData.MINE_TYPE, false);
                break;
        }
        if (save)
            saveConfig();
    }

    public void setName(String newName) {
        this.name = newName;
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

            switch (this.getBlockSetMode()) {
                case INCREMENTAL:
                    ++this.blockSetIndex;
                    if (this.blockSetIndex >= this.blockSets.size()) {
                        this.blockSetIndex = 0;
                    }
                    break;
                case RANDOM:
                    this.blockSetIndex = RealMinesAPI.getRand().nextInt(this.blockSets.size());
                    break;
                case NONE:
                    break;
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

    public Collection<RMBlockSet> getBlockSets() {
        return Collections.unmodifiableCollection(this.blockSets.values());
    }

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
        if (this.getMineCuboid() == null) {
            return Collections.emptyList();
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> this.getMineCuboid().contains(p.getLocation()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Location> getHighlightedCube() {
        final List<Location> result = new ArrayList<>();
        final World world = this.getMineCuboid().getPOS1().getWorld();
        final double minX = Math.min(this.getMineCuboid().getPOS1().getX(), this.getMineCuboid().getPOS2().getX());
        final double minY = Math.min(this.getMineCuboid().getPOS1().getY(), this.getMineCuboid().getPOS2().getY());
        final double minZ = Math.min(this.getMineCuboid().getPOS1().getZ(), this.getMineCuboid().getPOS2().getZ());
        final double maxX = Math.max(this.getMineCuboid().getPOS1().getX() + 1, this.getMineCuboid().getPOS2().getX() + 1);
        final double maxY = Math.max(this.getMineCuboid().getPOS1().getY() + 1, this.getMineCuboid().getPOS2().getY() + 1);
        final double maxZ = Math.max(this.getMineCuboid().getPOS1().getZ() + 1, this.getMineCuboid().getPOS2().getZ() + 1);
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
        saveData(MineData.RESET);
    }

    public void setResetState(final Reset e, final boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
        }
        saveData(MineData.RESET);
    }

    public void setResetValue(final Reset e, final int d) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentageValue = d;
                break;
            case TIME:
                this.resetByTimeValue = d;
                break;
        }
        saveData(MineData.RESET);
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
        saveData(MineData.TELEPORT);
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

    public Map<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    public abstract RMine.Type getType();

    public abstract void clearContents();

    public boolean isFreezed() {
        return this.freezed;
    }

    public void setFreezed(boolean freezed) {
        this.freezed = freezed;
    }

    public String getBreakPermission() {
        return "realmines." + this.getName() + ".break";
    }
}
