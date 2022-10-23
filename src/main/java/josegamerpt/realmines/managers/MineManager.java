package josegamerpt.realmines.managers;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.events.MineBlockBreakEvent;
import josegamerpt.realmines.mines.*;
import josegamerpt.realmines.mines.components.MineBlock;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.mines.gui.MineIcon;
import josegamerpt.realmines.mines.mine.BlockMine;
import josegamerpt.realmines.mines.mine.SchematicMine;
import josegamerpt.realmines.mines.tasks.MineResetTask;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class MineManager {

    public final List<String> signset = Arrays.asList("PM", "PL", "BM", "BR");
    private ArrayList<RMine> mines = new ArrayList<>();

    private static ArrayList<MineBlock> getBlocks(String s) {
        ArrayList<MineBlock> list = new ArrayList<>();
        for (String a : Mines.file().getStringList(s + ".Blocks")) {
            String[] content = a.split(";");
            Material mat = Material.valueOf(content[0]);
            Double per = Double.parseDouble(content[1]);
            list.add(new MineBlock(mat, per));
        }
        return list;
    }

    public ArrayList<String> getRegisteredMines() {
        Mines.reload();
        ArrayList<String> ret = new ArrayList<>();
        mines.forEach(s -> ret.add(s.getName()));
        return ret;
    }

    public void unregisterMine(RMine m) {
        Mines.file().set(m.getName(), null);
        Mines.save();
        this.mines.remove(m);
    }

    public void loadMines() {
        for (String s : Mines.file().getConfigurationSection("").getKeys(false)) {
            World w = Bukkit.getWorld(Mines.file().getString(s + ".World"));

            Location pos1 = new Location(w, Mines.file().getDouble(s + ".POS1.X"),
                    Mines.file().getDouble(s + ".POS1.Y"), Mines.file().getDouble(s + ".POS1.Z"));
            Location pos2 = new Location(w, Mines.file().getDouble(s + ".POS2.X"),
                    Mines.file().getDouble(s + ".POS2.Y"), Mines.file().getDouble(s + ".POS2.Z"));
            Location tp = null;

            if (Mines.file().get(s + ".Teleport") != null) {
                tp = new Location(w, Mines.file().getDouble(s + ".Teleport.X"),
                        Mines.file().getDouble(s + ".Teleport.Y"), Mines.file().getDouble(s + ".Teleport.Z"),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Yaw")),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Pitch")));
            }
            ArrayList<MineSign> signs = new ArrayList<>();
            if (Mines.file().get(s + ".Signs") != null) {
                for (String sig : Mines.file().getStringList(s + ".Signs")) {
                    String[] parse = sig.split(";");
                    World sigw = Bukkit.getWorld(parse[0]);
                    Location loc = new Location(w, Double.parseDouble(parse[1]), Double.parseDouble(parse[2]),
                            Double.parseDouble(parse[3]));
                    String mod = parse[4];
                    MineSign ms = new MineSign(sigw.getBlockAt(loc), mod);
                    signs.add(ms);
                }
            }

            HashMap<MineCuboid.CuboidDirection, Material> faces = new HashMap<>();
            if (Mines.file().get(s + ".Faces") != null) {
                for (String sig : Mines.file().getConfigurationSection(s + ".Faces").getKeys(false)) {
                    faces.put(MineCuboid.CuboidDirection.valueOf(sig), Material.valueOf(Mines.file().getString(s + ".Faces." + sig)));
                }
            }

            Material ic = Material.valueOf(Mines.file().getString(s + ".Icon"));
            ArrayList<MineBlock> blocks = getBlocks(s);

            String color = "white";

            if (!Mines.file().getString(s + ".Color").equals("")) {
                color = Mines.file().getString(s + ".Color");
            }

            Boolean saveType = false;

            String mtyp = Mines.file().getString(s + ".Type");
            RMine.Type type;
            if (mtyp == null || mtyp == "")
            {
                type = RMine.Type.BLOCKS;
                RealMines.getInstance().log(Level.WARNING, s + " converted into the new mine type.");
                saveType = true;
            } else {
                type = RMine.Type.valueOf(mtyp);
            }

            RMine m;
            switch (type)
            {
                case BLOCKS:
                    m = new BlockMine(s, Mines.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                            Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"), color, faces,
                            Mines.file().getBoolean(s + ".Settings.Reset.Silent"));
                    break;
                case SCHEMATIC:
                    Location place = new Location(w, Mines.file().getDouble(s + ".Place.X"),
                            Mines.file().getDouble(s + ".Place.Y"), Mines.file().getDouble(s + ".Place.Z"));
                    m = new SchematicMine(s, Mines.file().getString(s + ".Display-Name"), signs, place, Mines.file().getString(s + ".Schematic-Filename"), ic, tp,
                            Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                            Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                            Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                            Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"), color, pos1, pos2,
                            Mines.file().getBoolean(s + ".Settings.Reset.Silent"));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            m.register();
            if (saveType)
            {
                m.saveData(RMine.Data.MINE_TYPE);
            }
        }
    }

    public void createMine(Player p, String name) {
        WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                BlockMine m = new BlockMine(name, name, new ArrayList<>(), new ArrayList<>(), pos1, pos2,
                        Material.DIAMOND_ORE, null, false, true, 20, 60, "white", new HashMap<>(), false);
                m.saveAll();

                m.register();
                m.addBlock(new MineBlock(Material.STONE, 1D));
                m.reset();
                m.setTeleport(p.getLocation());
                m.saveData(RMine.Data.TELEPORT);

                ArrayList<Material> mat = m.getMineCuboid().getBlockTypes();
                if (mat.size() > 0) {
                    Text.send(p, Language.file().getString("System.Add-Blocks"));
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    Text.send(p, Language.file().getString("System.Block-Count").replaceAll("%count%", mat.size() + ""));

                    new PlayerInput(p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addBlock(new MineBlock(material, 0.1D)));
                            Text.send(p, Language.file().getString("System.Blocks-Added").replaceAll("%count%", mat.size() + ""));
                        }
                        Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name));
                    }, input -> Text.send(p, Language.file().getString("System.Mine-Created").replaceAll("%mine%", name)));
                }
            }
        } catch (Exception e) {
            Text.send(p, Language.file().getString("System.Boundaries-Not-Set"));
        }
    }

    public void createSchematicMine(Player p, String name) {
        Text.send(p, Language.file().getString("System.Input-Schematic"));

        new PlayerInput(p, s -> {
            File folder = new File(RealMines.getInstance().getDataFolder(), "schematics");
            File file = new File(folder, s);

            if (file.exists())
            {

                Location loc = new Location(p.getWorld(), 0,0,0);
                Location loc2 = new Location(p.getWorld(), 0,0,0);

                SchematicMine m = new SchematicMine(name, name, new ArrayList<>(), p.getLocation(), s,
                        Material.DIAMOND_ORE, null, false, true, 20, 60, "white", loc, loc2, false);
                m.saveAll();

                m.register();
                m.reset();
                m.setTeleport(p.getLocation());
                m.saveData(RMine.Data.TELEPORT);
                Text.send(p, Language.file().getString("System.Input-Schematic-Warn").replaceAll("%action%", "/mine setregion " + ChatColor.stripColor(name)));
            } else {
                Text.send(p, Language.file().getString("System.Invalid-Schematic"));
            }
        }, s -> {

        });
    }

    public void saveMine(RMine mine) {
        for (RMine.Data value : RMine.Data.values()) {
            this.saveMine(mine, value);
        }
    }

    public void saveMine(RMine mine, BlockMine.Data t) {
        switch (t) {
            case NAME:
                Mines.file().set(mine.getName() + ".Display-Name", mine.getDisplayName());
                break;
            case COLOR:
                Mines.file().set(mine.getName() + ".Color", mine.getColor().name());
                break;
            case BLOCKS:
                switch (mine.getType())
                {
                    case SCHEMATIC:
                        Mines.file().set(mine.getName() + ".Schematic-Filename", mine.getSchematicFilename());
                        break;
                    default:
                        Mines.file().set(mine.getName() + ".Blocks", mine.getBlockList());
                        break;
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
                Mines.file().set(mine.getName() + ".Settings.Reset.Silent", mine.isSilent());
                break;
            case PLACE:
                switch (mine.getType())
                {
                    case SCHEMATIC:
                        Mines.file().set(mine.getName() + ".World", mine.getSchematicPlace().getWorld().getName());
                        Mines.file().set(mine.getName() + ".Place.X", mine.getSchematicPlace().getX());
                        Mines.file().set(mine.getName() + ".Place.Y", mine.getSchematicPlace().getY());
                        Mines.file().set(mine.getName() + ".Place.Z", mine.getSchematicPlace().getZ());
                    default:
                        Mines.file().set(mine.getName() + ".World", mine.getPOS1().getWorld().getName());
                        Mines.file().set(mine.getName() + ".POS1.X", mine.getPOS1().getX());
                        Mines.file().set(mine.getName() + ".POS1.Y", mine.getPOS1().getY());
                        Mines.file().set(mine.getName() + ".POS1.Z", mine.getPOS1().getZ());
                        Mines.file().set(mine.getName() + ".POS2.X", mine.getPOS2().getX());
                        Mines.file().set(mine.getName() + ".POS2.Y", mine.getPOS2().getY());
                        Mines.file().set(mine.getName() + ".POS2.Z", mine.getPOS2().getZ());
                        break;
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
                if (mine.getType() != BlockMine.Type.SCHEMATIC) {
                    Mines.file().set(mine.getName() + ".Faces", null);
                    Iterator<Map.Entry<MineCuboid.CuboidDirection, Material>> it = mine.getFaces().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<MineCuboid.CuboidDirection, Material> pair = it.next();
                        Mines.file().set(mine.getName() + ".Faces." + pair.getKey().name(), pair.getValue().name());
                    }
                }
                break;
            case MINE_TYPE:
                Mines.file().set(mine.getName() + ".Type", mine.getType().name());
                break;
        }

        Mines.save();
    }

    public ArrayList<MineIcon> getMineList() {
        ArrayList<MineIcon> l = new ArrayList<>();
        mines.forEach(mine -> l.add(new MineIcon(mine)));
        if (l.size() == 0) {
            l.add(new MineIcon());
        }
        return l;
    }

    //permission for teleport: realmines.tp.<name>
    public void teleport(Player target, RMine m, Boolean silent) {
        if (!silent) {
            if (m.hasTP()) {
                if (target.hasPermission("realmines.tp." + m.getName())) {
                    target.teleport(m.getTeleport());

                    if (Config.file().getBoolean("RealMines.teleportMessage"))
                    {
                        Text.send(target, Language.file().getString("Mines.Teleport").replaceAll("%mine%", m.getDisplayName()));
                    }
                } else {
                    if (Config.file().getBoolean("RealMines.teleportMessage"))
                    {
                        Text.send(target, RealMines.getInstance().getPrefix() + Language.file().getString("System.Error-Permission"));
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

    public RMine get(String name) {
        return this.mines.stream().filter(o -> o.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void findBlockUpdate(Block b) {
        for (RMine m : this.mines) {
            if (m.getMineCuboid().contains(b)) {
                Bukkit.getPluginManager().callEvent(new MineBlockBreakEvent(m));
            }
        }
    }

    public void resetPercentage(RMine m) {
        m.updateSigns();
        if (m.isResetBy(RMine.Reset.PERCENTAGE) & ((double) m.getRemainingBlocksPer() < m.getResetValue(RMine.Reset.PERCENTAGE))) {
            m.kickPlayers(Language.file().getString("Mines.Reset.Percentage"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getInstance(), m::reset, 10);
        }
    }

    public ArrayList<MineSign> getSigns() {
        ArrayList<MineSign> l = new ArrayList<>();
        this.mines.forEach(mine -> l.addAll(mine.getSigns()));
        return l;
    }

    public void unloadMines() {
        this.mines.forEach(mine -> mine.getTimer().kill());
        this.clearMemory();
    }

    public void setRegion(String name, Player p) {
        RMine m = this.get(name);

        WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                m.setPOS(pos1, pos2);
                m.saveData(RMine.Data.PLACE);
                Text.send(p, Language.file().getString("System.Region-Updated"));
                m.reset();
            }
        } catch (Exception e) {
            Text.send(p, Language.file().getString("System.Boundaries-Not-Set"));
        }
    }

    public void stopTasks() {
        this.mines.forEach(mine -> mine.getTimer().kill());
    }

    public void startTasks() {
        this.mines.forEach(mine -> mine.getTimer().start());
    }

    public void deleteMine(RMine mine) {
        if (mine != null) {
            mine.clear();
            mine.getTimer().kill();
            mine.removeDependencies();
            for (MineResetTask task : RealMines.getInstance().getMineResetTasksManager().tasks) {
                if (task.hasMine(mine)) {
                    task.removeMine(mine);
                }
            }
        }
        this.unregisterMine(mine);
    }

    public void clearMemory() {
        this.mines.clear();
    }

    public ArrayList<RMine> getMines() {
        return this.mines;
    }

    public void add(RMine mine) {
        this.mines.add(mine);
    }
}
