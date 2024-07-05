package joserodpt.realmines.plugin.managers;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.event.MineBlockBreakEvent;
import joserodpt.realmines.api.event.RealMinesMineChangeEvent;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.MineIcon;
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
import joserodpt.realmines.api.mine.task.MineResetTask;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.types.SchematicMine;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.ItemStackSpringer;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMinesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MineManager extends MineManagerAPI {

    private final RealMinesAPI rm;
    public final List<String> signset = Arrays.asList("pm", "pl", "bm", "br");
    private final Map<String, RMine> mines = new HashMap<>();

    public MineManager(RealMinesAPI rm) {
        this.rm = rm;
    }

    @Override
    protected Map<Material, MineItem> getBlocks(final String mineName, final RMine.Type type) {
        final Map<Material, MineItem> list = new HashMap<>();

        if (RMMinesConfig.file().isList(mineName + ".Blocks")) {
            RealMinesPlugin.getPlugin().getLogger().warning("Starting block conversion from pre 1.6 version...");
            /*since version 1.6, blocks have a new format
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

            for (final String a : RMMinesConfig.file().getStringList(mineName + ".Blocks")) {
                final String[] content = a.split(";");
                final Double per = Double.parseDouble(content[1]);
                final String mat = content[0];
                try {
                    Material m = Material.valueOf(mat);
                    switch (type) {
                        case BLOCKS:
                            try {
                                list.put(m, new MineBlockItem(m, per));
                            } catch (Exception e) {
                                Bukkit.getLogger().severe("[RealMines] Material type" + mat + " is invalid! Skipping. This material is in mine: " + mineName);
                                continue;
                            }
                            break;
                        case FARM:
                            list.put(m, new MineFarmItem(FarmItem.valueOf(mat), per, Integer.parseInt(content[2])));
                            break;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[RealMines] Material type" + mat + " is invalid! Skipping. This material is in mine: " + mineName);
                }

            }

            //remove old config and save new
            RMMinesConfig.file().remove(mineName + ".Blocks");
            for (MineItem mineItem : list.values()) {
                switch (mineItem.getType()) {
                    case FARM:
                        RMMinesConfig.file().set(mineName + ".Blocks." + mineItem.getMaterial().name() + ".Age", ((MineFarmItem) mineItem).getAge());
                    case BLOCK:
                        RMMinesConfig.file().set(mineName + ".Blocks." + mineItem.getMaterial().name() + ".Chance", mineItem.getPercentage());
                        break;
                }
            }
            RMMinesConfig.save();

            RealMinesPlugin.getPlugin().getLogger().warning("Conversion finished with success.");
        } else {
            //since version 1.6, there's a new way to load the blocks
            if (RMMinesConfig.file().getSection(mineName + ".Blocks") == null) {
                return list;
            }

            for (final String mat : RMMinesConfig.file().getSection(mineName + ".Blocks").getRoutesAsStrings(false)) {
                final Double per = RMMinesConfig.file().getDouble(mineName + ".Blocks." + mat + ".Chance");
                final Boolean disabledVanillaDrop = RMMinesConfig.file().getBoolean(mineName + ".Blocks." + mat + ".Disabled-Vanilla-Drop");
                final Boolean disabledBlockMining = RMMinesConfig.file().getBoolean(mineName + ".Blocks." + mat + ".Disabled-Block-Mining");

                try {
                    Material m = Material.valueOf(mat);

                    List<MineAction> actionsList = new ArrayList<>();

                    if (RMMinesConfig.file().getSection(mineName + ".Blocks." + mat).getKeys().contains("Break-Actions")) {
                        for (final String actionID : RMMinesConfig.file().getSection(mineName + ".Blocks." + mat + ".Break-Actions").getRoutesAsStrings(false)) {
                            final String actionRoute = mineName + ".Blocks." + mat + ".Break-Actions." + actionID;
                            final Double chance = RMMinesConfig.file().getDouble(actionRoute + ".Chance");
                            try {
                                MineAction.Type mineactiontype = MineAction.Type.valueOf(RMMinesConfig.file().getString(actionRoute + ".Type"));
                                switch (mineactiontype) {
                                    case EXECUTE_COMMAND:
                                        actionsList.add(new MineActionCommand(actionID, mineName, chance, RMMinesConfig.file().getString(actionRoute + ".Command")));
                                        break;
                                    case DROP_ITEM:
                                        String data = RMMinesConfig.file().getString(actionRoute + ".Item");
                                        try {
                                            actionsList.add(new MineActionDropItem(actionID, mineName, chance, ItemStackSpringer.getItemDeSerializedJSON(data).clone()));
                                        } catch (Exception e) {
                                            rm.getPlugin().getLogger().severe("Badly formatted ItemStack: " + data);
                                            rm.getPlugin().getLogger().warning("Item Serialized for " + mat + " isn't valid! Skipping.");
                                            continue;
                                        }
                                        break;
                                    case GIVE_ITEM:
                                        String data2 = RMMinesConfig.file().getString(actionRoute + ".Item");
                                        try {
                                            actionsList.add(new MineActionGiveItem(actionID, mineName, chance, ItemStackSpringer.getItemDeSerializedJSON(data2)));
                                        } catch (Exception e) {
                                            rm.getPlugin().getLogger().severe("Badly formatted ItemStack: " + data2);
                                            rm.getPlugin().getLogger().warning("Item Serialized for " + mat + " isn't valid! Skipping.");
                                            continue;
                                        }
                                        break;
                                    case GIVE_MONEY:
                                        if (rm.getEconomy() == null) {
                                            rm.getPlugin().getLogger().warning("Money Break Action for " + mat + " will be ignored because Vault isn't installed on this server.");
                                            continue;
                                        }

                                        actionsList.add(new MineActionMoney(actionID, mineName, chance, RMMinesConfig.file().getDouble(actionRoute + ".Amount")));
                                        break;
                                }
                            } catch (Exception e) {
                                rm.getPlugin().getLogger().severe("Break Action Type " + RMMinesConfig.file().getString(actionRoute + ".Type") + " is invalid! Skipping. This action is in mine: " + mineName);
                            }
                        }
                    }

                    switch (type) {
                        case BLOCKS:
                            list.put(m, new MineBlockItem(m, per, disabledVanillaDrop, disabledBlockMining, actionsList));
                            break;
                        case FARM:
                            list.put(m, new MineFarmItem(FarmItem.valueOf(mat), per, disabledVanillaDrop, disabledBlockMining, RMMinesConfig.file().getInt(mineName + ".Blocks." + mat + ".Age"), actionsList));
                            break;
                        case SCHEMATIC:
                            list.put(m, new MineSchematicItem(m, disabledVanillaDrop, disabledBlockMining, actionsList));
                            break;
                    }
                } catch (Exception e) {
                    rm.getPlugin().getLogger().severe("Material type " + mat + " is invalid! Skipping. This material is in mine: " + mineName);
                }
            }
        }


        return list;
    }

    @Override
    public List<String> getRegisteredMines() {
        return this.getMines().values().stream()
                .map(RMine::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void loadMines() {
        for (final String s : RMMinesConfig.file().getRoot().getRoutesAsStrings(false)) {
            final String worldName = RMMinesConfig.file().getString(s + ".World");

            if (worldName == null || worldName.isEmpty()) {
                Bukkit.getLogger().severe("[RealMines] Could not load world " + worldName + ". Is the world name correct and valid? Skipping mine named: " + s);
                continue;
            }

            final World w = Bukkit.getWorld(worldName);

            if (w == null) {
                Bukkit.getLogger().severe("[RealMines] Could not load world " + worldName + ". Does the world exist and is loaded? Skipping mine named: " + s);
                continue;
            }

            final Location pos1 = new Location(w, RMMinesConfig.file().getDouble(s + ".POS1.X"),
                    RMMinesConfig.file().getDouble(s + ".POS1.Y"), RMMinesConfig.file().getDouble(s + ".POS1.Z"));
            final Location pos2 = new Location(w, RMMinesConfig.file().getDouble(s + ".POS2.X"),
                    RMMinesConfig.file().getDouble(s + ".POS2.Y"), RMMinesConfig.file().getDouble(s + ".POS2.Z"));
            Location tp = null;

            if (RMMinesConfig.file().get(s + ".Teleport") != null) {
                tp = new Location(w, RMMinesConfig.file().getDouble(s + ".Teleport.X"),
                        RMMinesConfig.file().getDouble(s + ".Teleport.Y"), RMMinesConfig.file().getDouble(s + ".Teleport.Z"),
                        Float.parseFloat(RMMinesConfig.file().getString(s + ".Teleport.Yaw")),
                        Float.parseFloat(RMMinesConfig.file().getString(s + ".Teleport.Pitch")));
            }

            final List<MineSign> signs = new ArrayList<>();
            if (RMMinesConfig.file().get(s + ".Signs") != null) {
                for (final String sig : RMMinesConfig.file().getStringList(s + ".Signs")) {
                    final String[] parse = sig.split(";");
                    final World sigw = Bukkit.getWorld(parse[0]);
                    final Location loc = new Location(w, Double.parseDouble(parse[1]), Double.parseDouble(parse[2]),
                            Double.parseDouble(parse[3]));
                    final String mod = parse[4];
                    final MineSign ms = new MineSign(sigw.getBlockAt(loc), mod);
                    signs.add(ms);
                }
            }

            final HashMap<MineCuboid.CuboidDirection, Material> faces = new HashMap<>();
            if (RMMinesConfig.file().isSection(s + ".Faces")) {
                for (final String sig : RMMinesConfig.file().getSection(s + ".Faces").getRoutesAsStrings(false)) {
                    faces.put(MineCuboid.CuboidDirection.valueOf(sig), Material.valueOf(RMMinesConfig.file().getString(s + ".Faces." + sig)));
                }
            }

            final Material ic = Material.valueOf(RMMinesConfig.file().getString(s + ".Icon"));

            MineColor mineColor = MineColor.WHITE;
            String color = RMMinesConfig.file().getString(s + ".Color");
            if (color != null && !color.isEmpty()) {
                mineColor = MineColor.valueOf(color);
            }

            boolean saveType = false;

            final String mtyp = RMMinesConfig.file().getString(s + ".Type");
            final String type;
            if (mtyp == null || mtyp.isEmpty()) {
                type = "BLOCKS";
                rm.getPlugin().getLogger().warning(s + " converted into the new mine block type.");
                saveType = true;
            } else {
                type = mtyp;
            }

            if (!RMMinesConfig.file().contains(s + ".Reset-Commands")) {
                RMMinesConfig.file().set(s + ".Reset-Commands", Collections.emptyList());
                RMMinesConfig.save();
            }

            final Map<Material, MineItem> blocks = getBlocks(s, RMine.Type.valueOf(type));

            final RMine m;
            switch (type) {
                case "BLOCKS":
                    m = new BlockMine(w, s, RMMinesConfig.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, faces,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.Silent"), RMMinesConfig.file().getBoolean(s + ".Settings.Break-Permission"), this);
                    break;
                case "SCHEMATIC":
                    final Location place = new Location(w, RMMinesConfig.file().getDouble(s + ".Place.X"),
                            RMMinesConfig.file().getDouble(s + ".Place.Y"), RMMinesConfig.file().getDouble(s + ".Place.Z"));
                    m = new SchematicMine(w, s, RMMinesConfig.file().getString(s + ".Display-Name"), blocks, signs, place, RMMinesConfig.file().getString(s + ".Schematic-Filename"), ic, tp,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, faces,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.Silent"), RMMinesConfig.file().getBoolean(s + ".Settings.Break-Permission"), this);
                    break;
                case "FARM":
                    m = new FarmMine(w, s, RMMinesConfig.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            RMMinesConfig.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, faces,
                            RMMinesConfig.file().getBoolean(s + ".Settings.Reset.Silent"), RMMinesConfig.file().getBoolean(s + ".Settings.Break-Permission"), this);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            this.addMine(m);
            if (saveType) {
                m.saveData(RMine.Data.MINE_TYPE);
            }
        }
    }

    @Override
    public void createMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                final BlockMine m = new BlockMine(p.getWorld(), name, name, new HashMap<>(), new ArrayList<>(), pos1, pos2,
                        Material.DIAMOND_ORE, null, false, true, 20, 60, MineColor.WHITE, new HashMap<>(), false, false, this);

                this.addMine(m);
                m.addItem(new MineBlockItem(Material.STONE, 1D));
                m.reset(RMine.ResetCause.CREATION);
                m.setTeleport(p.getLocation());

                m.saveAll();

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));

                final List<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    TranslatableLine.SYSTEM_ADD_BLOCKS.send(p);
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    TranslatableLine.SYSTEM_BLOCK_COUNT.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);


                    new PlayerInput(p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addItem(new MineBlockItem(material, 0.1D)));
                            TranslatableLine.SYSTEM_BLOCKS_ADDED.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);
                        }
                        TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p);
                    }, input -> TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p));
                }
            }
        } catch (final Exception ignored) {
            TranslatableLine.SYSTEM_BOUNDARIES_NOT_SET.send(p);
        }
    }

    @Override
    public void createCropsMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                if (pos1.getY() != pos2.getY()) {
                    //+1 in maximum point (pos1) because it has to count that block too
                    pos1.add(0, 1, 0);
                }

                final FarmMine m = new FarmMine(p.getWorld(), name, name, new HashMap<>(), new ArrayList<>(), pos1, pos2,
                        Material.WHEAT, null, false, true, 20, 60, MineColor.GREEN, new HashMap<>(), false, false, this);
                m.addFarmItem(new MineFarmItem(FarmItem.WHEAT, 1D));

                this.addMine(m);
                m.reset(RMine.ResetCause.CREATION);
                m.setTeleport(p.getLocation());

                m.saveAll();

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));

                final List<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    TranslatableLine.SYSTEM_ADD_BLOCKS.send(p);
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    TranslatableLine.SYSTEM_BLOCK_COUNT.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);

                    new PlayerInput(p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addFarmItem(new MineFarmItem(FarmItem.valueOf(Material.WHEAT))));
                            TranslatableLine.SYSTEM_BLOCKS_ADDED.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);
                        }
                        TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p);
                    }, input -> TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p));
                }
            }
        } catch (final Exception ignored) {
            TranslatableLine.SYSTEM_BOUNDARIES_NOT_SET.send(p);
        }
    }

    @Override
    public void createSchematicMine(final Player p, final String name) {
        TranslatableLine.SYSTEM_INPUT_SCHEMATIC.send(p);

        new PlayerInput(p, s -> {
            final File folder = new File(rm.getPlugin().getDataFolder(), "schematics");
            final File file = new File(folder, s);

            if (file.exists()) {
                final SchematicMine m = new SchematicMine(p.getWorld(), name, name, new ArrayList<>(), p.getLocation(), s,
                        Material.FILLED_MAP, null, false, true, 20, 60, MineColor.ORANGE, new HashMap<>(), false, false, this);

                this.addMine(m);
                m.setTeleport(p.getLocation());
                m.reset(RMine.ResetCause.CREATION);
                m.saveAll();

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));
            } else {
                TranslatableLine.SYSTEM_INVALID_SCHEMATIC.send(p);
            }
        }, s -> {

        });
    }

    @Override
    public void saveAllMineData(final RMine mine) {
        for (final RMine.Data value : RMine.Data.values()) {
            this.saveMine(mine, value);
        }
    }

    @Override
    public void saveMine(final RMine mine, final RMine.Data t) {
        switch (t) {
            case NAME:
                RMMinesConfig.file().set(mine.getName() + ".Display-Name", mine.getDisplayName());
                break;
            case COLOR:
                RMMinesConfig.file().set(mine.getName() + ".Color", mine.getMineColor().name());
                break;
            case BLOCKS:
                if (Objects.requireNonNull(mine.getType()) == RMine.Type.SCHEMATIC) {
                    RMMinesConfig.file().set(mine.getName() + ".Schematic-Filename", ((SchematicMine) mine).getSchematicFilename());
                }

                RMMinesConfig.file().remove(mine.getName() + ".Blocks");
                for (MineItem mineItem : mine.getMineItems().values()) {
                    RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Chance", mineItem.getPercentage());
                    if (mineItem.areVanillaDropsDisabled()) {
                        RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Disabled-Vanilla-Drop", true);
                    }
                    if (mineItem.isBlockMiningDisabled()) {
                        RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Disabled-Block-Mining", true);
                    }

                    if (mine.getType() == RMine.Type.FARM) {
                        RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Age", ((MineFarmItem) mineItem).getAge());
                    }
                    if (!mineItem.getBreakActions().isEmpty()) {
                        for (MineAction ba : mineItem.getBreakActions()) {
                            RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Break-Actions." + ba.getID() + ".Type", ba.getType().name());
                            RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Break-Actions." + ba.getID() + ".Chance", ba.getChance());
                            switch (ba.getType()) {
                                case EXECUTE_COMMAND:
                                    RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Break-Actions." + ba.getID() + ".Command", ba.getValue());
                                    break;
                                case GIVE_MONEY:
                                    RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Break-Actions." + ba.getID() + ".Amount", ba.getValue());
                                    break;
                                case GIVE_ITEM:
                                case DROP_ITEM:
                                    RMMinesConfig.file().set(mine.getName() + ".Blocks." + mineItem.getMaterial().name() + ".Break-Actions." + ba.getID() + ".Item", ba.getValue());
                                    break;
                            }
                        }
                    }
                }
                break;
            case ICON:
                RMMinesConfig.file().set(mine.getName() + ".Icon", mine.getIcon().name());
                break;
            case SETTINGS:
                RMMinesConfig.file().set(mine.getName() + ".Settings.Break-Permission", mine.isBreakingPermissionOn());
                RMMinesConfig.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(RMine.Reset.PERCENTAGE));
                RMMinesConfig.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(RMine.Reset.TIME));
                RMMinesConfig.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(RMine.Reset.PERCENTAGE));
                RMMinesConfig.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(RMine.Reset.TIME));
                RMMinesConfig.file().set(mine.getName() + ".Settings.Reset.Silent", mine.isSilent());
                break;
            case LOCATION:
                if (mine.getType() == RMine.Type.SCHEMATIC) {
                    RMMinesConfig.file().set(mine.getName() + ".World", ((SchematicMine) mine).getSchematicPlace().getWorld().getName());
                    RMMinesConfig.file().set(mine.getName() + ".Place.X", ((SchematicMine) mine).getSchematicPlace().getX());
                    RMMinesConfig.file().set(mine.getName() + ".Place.Y", ((SchematicMine) mine).getSchematicPlace().getY());
                    RMMinesConfig.file().set(mine.getName() + ".Place.Z", ((SchematicMine) mine).getSchematicPlace().getZ());
                } else {
                    RMMinesConfig.file().set(mine.getName() + ".World", mine.getPOS1().getWorld().getName());
                    RMMinesConfig.file().set(mine.getName() + ".POS1.X", mine.getPOS1().getX());
                    RMMinesConfig.file().set(mine.getName() + ".POS1.Y", mine.getPOS1().getY());
                    RMMinesConfig.file().set(mine.getName() + ".POS1.Z", mine.getPOS1().getZ());
                    RMMinesConfig.file().set(mine.getName() + ".POS2.X", mine.getPOS2().getX());
                    RMMinesConfig.file().set(mine.getName() + ".POS2.Y", mine.getPOS2().getY());
                    RMMinesConfig.file().set(mine.getName() + ".POS2.Z", mine.getPOS2().getZ());
                }
                break;
            case TELEPORT:
                if (mine.getTeleport() != null) {
                    RMMinesConfig.file().set(mine.getName() + ".Teleport.X", mine.getTeleport().getX());
                    RMMinesConfig.file().set(mine.getName() + ".Teleport.Y", mine.getTeleport().getY());
                    RMMinesConfig.file().set(mine.getName() + ".Teleport.Z", mine.getTeleport().getZ());
                    RMMinesConfig.file().set(mine.getName() + ".Teleport.Yaw", mine.getTeleport().getYaw());
                    RMMinesConfig.file().set(mine.getName() + ".Teleport.Pitch", mine.getTeleport().getPitch());
                }
                break;
            case SIGNS:
                RMMinesConfig.file().set(mine.getName() + ".Signs", mine.getSignList());
                break;
            case FACES:
                RMMinesConfig.file().set(mine.getName() + ".Faces", null);
                for (Map.Entry<MineCuboid.CuboidDirection, Material> pair : mine.getFaces().entrySet()) {
                    RMMinesConfig.file().set(mine.getName() + ".Faces." + pair.getKey().name(), pair.getValue().name());
                }
                break;
            case MINE_TYPE:
                RMMinesConfig.file().set(mine.getName() + ".Type", mine.getType().name());
                break;
        }

        RMMinesConfig.save();
    }

    @Override
    public List<MineIcon> getMineList() {
        return this.getMines().isEmpty() ? Collections.singletonList(new MineIcon()) : this.getMines().values().stream()
                .map(MineIcon::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //permission for teleport: realmines.tp.<name>
    @Override
    public void teleport(final Player target, final RMine m, final Boolean silent, final Boolean checkForPermission) {
        if (!silent) {
            if (m.hasTP()) {
                if (checkForPermission) {
                    if (target.hasPermission("realmines.tp." + m.getName())) {
                        target.teleport(m.getTeleport());

                        if (RMConfig.file().getBoolean("RealMines.teleportMessage")) {
                            TranslatableLine.MINE_TELEPORT.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(target);
                        }
                    } else {
                        if (RMConfig.file().getBoolean("RealMines.teleportMessage")) {
                            TranslatableLine.SYSTEM_ERROR_PERMISSION.send(target);
                        }
                    }
                } else {
                    target.teleport(m.getTeleport());
                }
            } else {
                TranslatableLine.MINE_NO_TELEPORT_LOCATION.send(target);
            }
        } else {
            if (m.hasTP()) {
                target.teleport(m.getTeleport());
            }
        }
    }

    @Override
    public RMine getMine(final String name) {
        return this.getMines().getOrDefault(name, null);
    }

    @Override
    public MineItem findBlockUpdate(final Player p, final Cancellable e, final Block block, final boolean broken) {
        for (final RMine mine : this.getMines().values()) {
            if (mine.getMineCuboid() == null) {
                continue;
            }

            if (mine.getMineCuboid().contains(block)) {
                if (mine.isFreezed() || (mine.isBreakingPermissionOn() && !p.hasPermission(mine.getBreakPermission()))) {
                    e.setCancelled(true);
                } else {
                    if (mine.getType() == RMine.Type.FARM && !FarmItem.getCrops().contains(block.getType())) {
                        e.setCancelled(true);
                    } else {
                        MineItem mi = mine.getMineItems().get(block.getType());
                        if (mi != null) {
                            if (mi.isBlockMiningDisabled()) {
                                e.setCancelled(true);
                            } else {
                                Bukkit.getPluginManager().callEvent(new MineBlockBreakEvent(p, mine, block, broken));
                                return mine.getMineItems().get(block.getType());
                            }
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public List<MineSign> getSigns() {
        return this.getMines().values().stream()
                .flatMap(mine -> mine.getSigns().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void unloadMines() {
        this.getMines().values().forEach(mine -> mine.getTimer().kill());
        this.clearMemory();
    }

    @Override
    public void setBounds(final RMine m, final Player p) {
        if (m.getType() == RMine.Type.SCHEMATIC) {
            return;
        }

        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                m.setPOS(pos1, pos2);
                m.fill();
                TranslatableLine.SYSTEM_REGION_UPDATED.send(p);
                m.reset(RMine.ResetCause.PLUGIN);
                m.saveData(RMine.Data.LOCATION);

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.BOUNDS_UPDATED));
            }
        } catch (final Exception e) {
            TranslatableLine.SYSTEM_BOUNDARIES_NOT_SET.send(p);
        }
    }

    @Override
    public void stopTasks() {
        this.getMines().values().forEach(mine -> mine.getTimer().kill());
    }

    @Override
    public void startTasks() {
        this.getMines().values().forEach(mine -> mine.getTimer().start());
    }

    @Override
    public void deleteMine(final RMine mine) {
        if (mine != null) {
            Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(mine, RealMinesMineChangeEvent.ChangeOperation.REMOVED));
            mine.clear();
            mine.getTimer().kill();
            mine.removeDependencies();
            for (final MineResetTask task : rm.getMineResetTasksManager().getTasks()) {
                if (task.hasMine(mine)) {
                    task.removeMine(mine);
                }
            }
            this.unregisterMine(mine);
        }
    }

    @Override
    public void clearMemory() {
        this.mines.clear();
    }

    @Override
    public Map<String, RMine> getMines() {
        return this.mines;
    }

    @Override
    public void addMine(final RMine mine) {
        this.mines.put(mine.getName(), mine);
    }

    @Override
    public File getSchematicFolder() {
        return rm.getPlugin().getDataFolder();
    }

    @Override
    public void renameMine(RMine m, String newName) {
        this.unregisterMine(m);
        m.setName(newName);
        m.setDisplayName(newName);
        this.registerMine(m);
    }

    @Override
    public void unregisterMine(final RMine m) {
        RMMinesConfig.file().remove(m.getName());
        RMMinesConfig.save();
        this.getMines().remove(m.getName());
    }

    @Override
    public void registerMine(final RMine m) {
        this.getMines().put(m.getName(), m);
        saveAllMineData(m);
    }
}
