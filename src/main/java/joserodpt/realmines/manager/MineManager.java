package joserodpt.realmines.manager;

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
import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.config.Mines;
import joserodpt.realmines.event.MineBlockBreakEvent;
import joserodpt.realmines.mine.components.MineColor;
import joserodpt.realmines.mine.icons.MineIcon;
import joserodpt.realmines.mine.icons.MineBlockItem;
import joserodpt.realmines.mine.icons.MineFarmItem;
import joserodpt.realmines.mine.icons.MineItem;
import joserodpt.realmines.mine.types.BlockMine;
import joserodpt.realmines.mine.types.farm.FarmItem;
import joserodpt.realmines.mine.types.farm.FarmMine;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.types.SchematicMine;
import joserodpt.realmines.mine.components.MineCuboid;
import joserodpt.realmines.mine.components.MineSign;
import joserodpt.realmines.mine.task.MineResetTask;
import joserodpt.realmines.util.PlayerInput;
import joserodpt.realmines.util.Text;
import joserodpt.realmines.util.converters.mrl.MRLconverter;
import joserodpt.realmines.util.converters.RMConverterBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MineManager {
    
    private RealMines rm;
    public final List<String> signset = Arrays.asList("pm", "pl", "bm", "br");
    private final Map<String, RMine> mines = new HashMap<>();
    private final Map<String, RMConverterBase> converters = new HashMap<>();

    public MineManager(RealMines rm) {
        this.rm = rm;

        this.converters.put("MRL", new MRLconverter(rm));
    }

    private static ArrayList<MineItem> getBlocks(final String s, final RMine.Type type) {
        final ArrayList<MineItem> list = new ArrayList<>();
        for (final String a : Mines.file().getStringList(s + ".Blocks")) {
            final String[] content = a.split(";");
            final Double per = Double.parseDouble(content[1]);
            switch (type) {
                case BLOCKS:
                    final Material mat = Material.valueOf(content[0]);
                    list.add(new MineBlockItem(mat, per));
                    break;
                case FARM:
                    list.add(new MineFarmItem(FarmItem.valueOf(content[0]), per, Integer.parseInt(content[2])));
                    break;
            }
        }
        return list;
    }

    public ArrayList<String> getRegisteredMines() {
        Mines.reload();
        return this.getMines().values().stream()
                .map(RMine::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void unregisterMine(final RMine m) {
        Mines.file().set(m.getName(), null);
        Mines.save();
        this.getMines().remove(m.getName());
    }

    public void loadMines() {
        for (final String s : Mines.file().getConfigurationSection("").getKeys(false)) {
            final World w = Bukkit.getWorld(Mines.file().getString(s + ".World"));

            final Location pos1 = new Location(w, Mines.file().getDouble(s + ".POS1.X"),
                    Mines.file().getDouble(s + ".POS1.Y"), Mines.file().getDouble(s + ".POS1.Z"));
            final Location pos2 = new Location(w, Mines.file().getDouble(s + ".POS2.X"),
                    Mines.file().getDouble(s + ".POS2.Y"), Mines.file().getDouble(s + ".POS2.Z"));
            Location tp = null;

            if (Mines.file().get(s + ".Teleport") != null) {
                tp = new Location(w, Mines.file().getDouble(s + ".Teleport.X"),
                        Mines.file().getDouble(s + ".Teleport.Y"), Mines.file().getDouble(s + ".Teleport.Z"),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Yaw")),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Pitch")));
            }
            final ArrayList<MineSign> signs = new ArrayList<>();
            if (Mines.file().get(s + ".Signs") != null) {
                for (final String sig : Mines.file().getStringList(s + ".Signs")) {
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
            if (Mines.file().get(s + ".Faces") != null) {
                for (final String sig : Mines.file().getConfigurationSection(s + ".Faces").getKeys(false)) {
                    faces.put(MineCuboid.CuboidDirection.valueOf(sig), Material.valueOf(Mines.file().getString(s + ".Faces." + sig)));
                }
            }

            final Material ic = Material.valueOf(Mines.file().getString(s + ".Icon"));

            MineColor mineColor = MineColor.WHITE;
            String color = Mines.file().getString(s + ".Color");
            if (color != null && !color.isEmpty()) {
                mineColor = MineColor.valueOf(color);
            }

            boolean saveType = false;

            final String mtyp = Mines.file().getString(s + ".Type");
            final String type;
            if (mtyp == null || mtyp == "") {
                type = "BLOCKS";
                rm.log(Level.WARNING, s + " converted into the new mine block type.");
                saveType = true;
            } else {
                type = mtyp;
            }

            final ArrayList<MineItem> blocks = getBlocks(s, RMine.Type.valueOf(type));

            final RMine m;
            switch (type) {
                case "BLOCKS":
                    m = new BlockMine(s, Mines.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                            Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, faces,
                            Mines.file().getBoolean(s + ".Settings.Reset.Silent"), this);
                    break;
                case "SCHEMATIC":
                    final Location place = new Location(w, Mines.file().getDouble(s + ".Place.X"),
                            Mines.file().getDouble(s + ".Place.Y"), Mines.file().getDouble(s + ".Place.Z"));
                    m = new SchematicMine(s, Mines.file().getString(s + ".Display-Name"), signs, place, Mines.file().getString(s + ".Schematic-Filename"), ic, tp,
                            Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, pos1, pos2, faces,
                            Mines.file().getBoolean(s + ".Settings.Reset.Silent"), this);
                    break;
                case "FARM":
                    m = new FarmMine(s, Mines.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                            Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"), mineColor, faces,
                            Mines.file().getBoolean(s + ".Settings.Reset.Silent"), this);
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

    public void createMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                final BlockMine m = new BlockMine(name, name, new ArrayList<>(), new ArrayList<>(), pos1, pos2,
                        Material.DIAMOND_ORE, null, false, true, 20, 60, MineColor.WHITE, new HashMap<>(), false, this);

                this.addMine(m);
                m.addItem(new MineBlockItem(Material.STONE, 1D));
                m.reset();
                m.setTeleport(p.getLocation());

                m.saveAll();

                final ArrayList<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    Text.send(p, Language.file().getString("System.Add-Blocks"));
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    Text.send(p, Language.file().getString("System.Block-Count").replaceAll("%count%", String.valueOf(mat.size())));

                    new PlayerInput(p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addItem(new MineBlockItem(material, 0.1D)));
                            Text.send(p, Language.file().getString("System.Blocks-Added").replaceAll("%count%", String.valueOf(mat.size())));
                        }
                        Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name));
                    }, input -> Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name)));
                }
            }
        } catch (final Exception e) {
            Text.send(p, Language.file().getString("System.Boundaries-Not-Set"));
            e.printStackTrace();
        }
    }

    public void createCropsMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                if (pos1.getY() != pos2.getY()) {
                    Text.send(p, Language.file().getString("System.Mine-Same-Height"));
                    return;
                }

                final FarmMine m = new FarmMine(name, name, new ArrayList<>(), new ArrayList<>(), pos1, pos2,
                        Material.WHEAT, null, false, true, 20, 60, MineColor.GREEN, new HashMap<>(), false, this);
                m.addFarmItem(new MineFarmItem(FarmItem.WHEAT, 1D));

                this.addMine(m);
                m.reset();
                m.setTeleport(p.getLocation());

                m.saveAll();

                final ArrayList<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    Text.send(p, Language.file().getString("System.Add-Blocks"));
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    Text.send(p, Language.file().getString("System.Block-Count").replaceAll("%count%", String.valueOf(mat.size())));

                    new PlayerInput(p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material ->   m.addFarmItem(new MineFarmItem(FarmItem.valueOf(Material.WHEAT))));
                            Text.send(p, Language.file().getString("System.Blocks-Added").replaceAll("%count%", String.valueOf(mat.size())));
                        }
                        Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name));
                    }, input -> Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name)));
                }
            }
        } catch (final Exception e) {
            Text.send(p, Language.file().getString("System.Boundaries-Not-Set"));
            e.printStackTrace();
        }
    }

    public void createSchematicMine(final Player p, final String name) {
        Text.send(p, Language.file().getString("System.Input-Schematic"));

        new PlayerInput(p, s -> {
            final File folder = new File(rm.getDataFolder(), "schematics");
            final File file = new File(folder, s);

            if (file.exists()) {
                final Location loc = new Location(p.getWorld(), 0, 0, 0);
                final Location loc2 = new Location(p.getWorld(), 0, 0, 0);

                final SchematicMine m = new SchematicMine(name, name, new ArrayList<>(), p.getLocation(), s,
                        Material.FILLED_MAP, null, false, true, 20, 60, MineColor.ORANGE, loc, loc2, new HashMap<>(), false, this);

                this.addMine(m);
                m.reset();
                m.setTeleport(p.getLocation());
                m.saveAll();

                Text.send(p, Language.file().getString("System.Input-Schematic-Warn").replaceAll("%action%", "/mine setregion " + ChatColor.stripColor(name)));
            } else {
                Text.send(p, Language.file().getString("System.Invalid-Schematic"));
            }
        }, s -> {

        });
    }

    public void saveMine(final RMine mine) {
        for (final RMine.Data value : RMine.Data.values()) {
            this.saveMine(mine, value);
        }
    }

    public void saveMine(final RMine mine, final RMine.Data t) {
        switch (t) {
            case NAME:
                Mines.file().set(mine.getName() + ".Display-Name", mine.getDisplayName());
                break;
            case COLOR:
                Mines.file().set(mine.getName() + ".Color", mine.getMineColor().name());
                break;
            case BLOCKS:
                if (mine instanceof SchematicMine) {
                    Mines.file().set(mine.getName() + ".Schematic-Filename", ((SchematicMine) mine).getSchematicFilename());
                } else {
                    switch (mine.getType()) {
                        case BLOCKS:
                            Mines.file().set(mine.getName() + ".Blocks", ((BlockMine) mine).getBlockList());
                            break;
                        case FARM:
                            Mines.file().set(mine.getName() + ".Blocks", ((FarmMine) mine).getFarmItems());
                            break;
                    }
                }
                break;
            case ICON:
                Mines.file().set(mine.getName() + ".Icon", mine.getIcon().name());
                break;
            case OPTIONS:
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(RMine.Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(RMine.Reset.TIME));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(RMine.Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(RMine.Reset.TIME));
                Mines.file().set(mine.getName() + ".Settings.Reset.Silent", mine.isResetBy(RMine.Reset.SILENT));
                break;
            case PLACE:
                if (mine instanceof SchematicMine) {
                    Mines.file().set(mine.getName() + ".World", ((SchematicMine) mine).getSchematicPlace().getWorld().getName());
                    Mines.file().set(mine.getName() + ".Place.X", ((SchematicMine) mine).getSchematicPlace().getX());
                    Mines.file().set(mine.getName() + ".Place.Y", ((SchematicMine) mine).getSchematicPlace().getY());
                    Mines.file().set(mine.getName() + ".Place.Z", ((SchematicMine) mine).getSchematicPlace().getZ());
                } else {
                    Mines.file().set(mine.getName() + ".World", mine.getPOS1().getWorld().getName());
                    Mines.file().set(mine.getName() + ".POS1.X", mine.getPOS1().getX());
                    Mines.file().set(mine.getName() + ".POS1.Y", mine.getPOS1().getY());
                    Mines.file().set(mine.getName() + ".POS1.Z", mine.getPOS1().getZ());
                    Mines.file().set(mine.getName() + ".POS2.X", mine.getPOS2().getX());
                    Mines.file().set(mine.getName() + ".POS2.Y", mine.getPOS2().getY());
                    Mines.file().set(mine.getName() + ".POS2.Z", mine.getPOS2().getZ());
                }

                break;
            case TELEPORT:
                if (mine.getTeleport() != null) {
                    Mines.file().set(mine.getName() + ".Teleport.X", mine.getTeleport().getX());
                    Mines.file().set(mine.getName() + ".Teleport.Y", mine.getTeleport().getY());
                    Mines.file().set(mine.getName() + ".Teleport.Z", mine.getTeleport().getZ());
                    Mines.file().set(mine.getName() + ".Teleport.Yaw", mine.getTeleport().getYaw());
                    Mines.file().set(mine.getName() + ".Teleport.Pitch", mine.getTeleport().getPitch());
                }
                break;
            case SIGNS:
                Mines.file().set(mine.getName() + ".Signs", mine.getSignList());
                break;
            case FACES:
                Mines.file().set(mine.getName() + ".Faces", null);
                for (Map.Entry<MineCuboid.CuboidDirection, Material> pair : mine.getFaces().entrySet()) {
                    Mines.file().set(mine.getName() + ".Faces." + pair.getKey().name(), pair.getValue().name());
                }
                break;
            case MINE_TYPE:
                Mines.file().set(mine.getName() + ".Type", mine.getType().name());
                break;
        }

        Mines.save();
    }

    public List<MineIcon> getMineList() {
        return this.getMines().isEmpty() ? Collections.singletonList(new MineIcon()) : this.getMines().values().stream()
                .map(MineIcon::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //permission for teleport: realmines.tp.<name>
    public void teleport(final Player target, final RMine m, final Boolean silent) {
        if (!silent) {
            if (m.hasTP()) {
                if (target.hasPermission("realmines.tp." + m.getName())) {
                    target.teleport(m.getTeleport());

                    if (Config.file().getBoolean("RealMines.teleportMessage")) {
                        Text.send(target, Language.file().getString("Mines.Teleport").replaceAll("%mine%", m.getDisplayName()));
                    }
                } else {
                    if (Config.file().getBoolean("RealMines.teleportMessage")) {
                        Text.send(target, Text.color(Config.file().getString("RealMines.Prefix")) + Language.file().getString("System.Error-Permission"));
                    }
                }
            } else {
                Text.send(target, Language.file().getString("Mines.No-Teleport-Location"));
            }
        } else {
            if (m.hasTP()) {
                target.teleport(m.getTeleport());
            }
        }
    }

    public RMine getMine(final String name) {
        return this.getMines().getOrDefault(name, null);
    }

    public void findBlockUpdate(final Cancellable e, final Block b, final boolean broken) {
        for (final RMine m : this.getMines().values()) {
            if (m.getMineCuboid().contains(b)) {
                if (m.isFreezed()) {
                    e.setCancelled(true);
                } else {
                    Bukkit.getPluginManager().callEvent(new MineBlockBreakEvent(m, broken));
                }
                return;
            }
        }
    }

    public ArrayList<MineSign> getSigns() {
        return this.getMines().values().stream()
                .flatMap(mine -> mine.getSigns().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void unloadMines() {
        this.getMines().values().forEach(mine -> mine.getTimer().kill());
        this.clearMemory();
    }

    public void setRegion(final String name, final Player p) {
        final RMine m = this.getMine(name);

        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                m.setPOS(pos1, pos2);
                m.saveData(RMine.Data.PLACE);
                Text.send(p, Language.file().getString("System.Region-Updated"));
                m.reset();
            }
        } catch (final Exception e) {
            Text.send(p, Language.file().getString("System.Boundaries-Not-Set"));
        }
    }

    public void stopTasks() {
        this.getMines().values().forEach(mine -> mine.getTimer().kill());
    }

    public void startTasks() {
        this.getMines().values().forEach(mine -> mine.getTimer().start());
    }

    public void deleteMine(final RMine mine) {
        if (mine != null) {
            mine.clear();
            mine.getTimer().kill();
            mine.removeDependencies();
            for (final MineResetTask task : rm.getMineResetTasksManager().tasks) {
                if (task.hasMine(mine)) {
                    task.removeMine(mine);
                }
            }
        }
        assert mine != null;
        this.unregisterMine(mine);
    }

    public void clearMemory() {
        this.mines.clear();
    }

    public Map<String, RMine> getMines() {
        return this.mines;
    }

    public void addMine(final RMine mine) {
        this.mines.put(mine.getName(), mine);
    }

    public File getSchematicFolder() {
        return rm.getDataFolder();
    }

    public Map<String, RMConverterBase> getConverters() {
        return this.converters;
    }
}
