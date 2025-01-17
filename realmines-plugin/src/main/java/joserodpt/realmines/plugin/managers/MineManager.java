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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMMinesOldConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.event.MineBlockBreakEvent;
import joserodpt.realmines.api.event.RealMinesMineChangeEvent;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineIcon;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.RMineSettings;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.task.MineResetTask;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.types.SchematicMine;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.gui.DirectoryBrowserGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MineManager extends MineManagerAPI {

    private final RealMinesAPI rm;
    public final List<String> signset = Arrays.asList("pm", "pl", "bm", "br");
    private final Map<String, RMine> mines = new HashMap<>();

    public MineManager(RealMinesAPI rm) {
        this.rm = rm;
    }

    @Override
    public List<String> getRegisteredMines() {
        return this.getMines().values().stream()
                .map(RMine::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void loadMines() {
        if (RMMinesOldConfig.fileExists() && RMMinesOldConfig.file() != null) {
            rm.getLogger().warning("Converting mines into the new format...");

            for (final String mineName : RMMinesOldConfig.file().getRoot().getRoutesAsStrings(false)) {
                rm.getLogger().warning("Converting mine " + mineName + " into the new format...");

                final String mtyp = RMMinesOldConfig.file().getString(mineName + ".Type");
                final String type;
                if (mtyp == null || mtyp.isEmpty()) {
                    type = "BLOCKS";
                    rm.getPlugin().getLogger().warning(mineName + " converted into the new mine block type.");
                } else {
                    type = mtyp;
                }

                Section mineConfigSection = RMMinesOldConfig.file().getSection(mineName);

                try {
                    switch (type) {
                        case "BLOCKS":
                            mines.put(mineName, new BlockMine(mineName, mineConfigSection));
                            break;
                        case "SCHEMATIC":
                            mines.put(mineName, new SchematicMine(mineName, mineConfigSection));
                            break;
                        case "FARM":
                            mines.put(mineName, new FarmMine(mineName, mineConfigSection));
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + type);
                    }
                } catch (RMFailedToLoadException e) {
                    rm.getLogger().severe("Failed to load mine " + mineName + "!");
                    rm.getLogger().severe("Error: " + e.getMessage());
                }

                rm.getLogger().info("Mine " + mineName + " converted into the new format!");
            }

            RMMinesOldConfig.delete();
            rm.getLogger().warning("Conversion finished with success.");
        } else {
            //new mine loading system
            File minesFolder = new File(rm.getPlugin().getDataFolder(), "mines");

            if (!minesFolder.exists()) {
                minesFolder.mkdirs();
            }

            for (final File file : minesFolder.listFiles()) {
                if (file.getName().endsWith(".yml")) {
                    try {
                        YamlConfiguration mineConfig = YamlConfiguration.loadConfiguration(file);
                        String mineName = mineConfig.getString("name");
                        String type = mineConfig.getString("type");

                        if (mineName == null || type == null) {
                            rm.getLogger().warning("Failed to load mine " + file.getName() + "!");
                            rm.getLogger().warning("Error: name or type not found.");
                            continue;
                        }

                        switch (type) {
                            case "BLOCKS":
                                addMine(new BlockMine(mineName, mineConfig));
                                break;
                            case "SCHEMATIC":
                                addMine(new SchematicMine(mineName, mineConfig));
                                break;
                            case "FARM":
                                addMine(new FarmMine(mineName, mineConfig));
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + type);
                        }
                    } catch (RMFailedToLoadException e) {
                        rm.getLogger().severe("Failed to load mine " + file.getName() + "!");
                        rm.getLogger().severe("Error: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void createMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                final BlockMine m = new BlockMine(name, p.getWorld(), pos1, pos2);

                m.addItem("default", new MineBlockItem(Material.STONE, 1D));
                m.reset(RMine.ResetCause.CREATION);
                m.setTeleport(p.getLocation());

                this.addMine(m);

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));

                final List<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    TranslatableLine.SYSTEM_ADD_BLOCKS.send(p);
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    TranslatableLine.SYSTEM_BLOCK_COUNT.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);

                    new PlayerInput(true, p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addItem("default", new MineBlockItem(material, 0.1D)));
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
    public void createFarmMine(final Player p, final String name) {
        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                if (pos1.getY() != pos2.getY()) {
                    //+1 in maximum point (pos1) because it has to count that block too
                    pos1.add(0, 1, 0);
                }

                final FarmMine m = new FarmMine(name, p.getWorld(), pos1, pos2);
                m.addFarmItem("default", new MineFarmItem(FarmItem.WHEAT, 1D));

                this.addMine(m);
                m.reset(RMine.ResetCause.CREATION);
                m.setTeleport(p.getLocation());

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));

                final List<Material> mat = m.getMineCuboid().getBlockTypes();
                if (!mat.isEmpty()) {
                    TranslatableLine.SYSTEM_ADD_BLOCKS.send(p);
                    mat.forEach(material -> Text.send(p, " &7> &f" + material.name()));
                    TranslatableLine.SYSTEM_BLOCK_COUNT.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);

                    new PlayerInput(true, p, input -> {
                        if (input.equalsIgnoreCase("yes")) {
                            mat.forEach(material -> m.addFarmItem("default", new MineFarmItem(FarmItem.valueOf(Material.WHEAT))));
                            TranslatableLine.SYSTEM_BLOCKS_ADDED.setV1(TranslatableLine.ReplacableVar.COUNT.eq(String.valueOf(mat.size()))).send(p);
                        }
                        TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p);
                    }, input -> TranslatableLine.SYSTEM_MINE_CREATED.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(p));
                }
            }
        } catch (final Exception ignored) {
            ignored.printStackTrace();
            TranslatableLine.SYSTEM_BOUNDARIES_NOT_SET.send(p);
        }
    }

    @Override
    public void createSchematicMine(final Player p, final String name) {
        final File pluginFolder = new File(rm.getPlugin().getDataFolder(), "schematics");

        DirectoryBrowserGUI dbg = new DirectoryBrowserGUI(p, pluginFolder, "Please select a schematic", Arrays.asList("schem", "schematic"), (file) -> {
            File finalFile = null;

            if (!file.getAbsolutePath().toLowerCase().contains("realmines")) {
                try {
                    if (pluginFolder.exists() && pluginFolder.isDirectory()) {
                        finalFile = new File(pluginFolder, file.getName());

                        Files.copy(file.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Bukkit.getLogger().warning("The plugin shematic's folder is not a directory or does not exist.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                finalFile = file;
            }

            if (finalFile != null && finalFile.exists()) {
                final SchematicMine m = new SchematicMine(name, p.getLocation(), finalFile.getName());

                this.addMine(m);
                m.setTeleport(p.getLocation());
                m.reset(RMine.ResetCause.CREATION);

                Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(m, RealMinesMineChangeEvent.ChangeOperation.ADDED));
            } else {

                TranslatableLine.SYSTEM_INVALID_SCHEMATIC.send(p);
            }
        });
        dbg.openInventory(p);
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
                if (mine.isFreezed() || (mine.getSettingBool(RMineSettings.BREAK_PERMISSION) && !p.hasPermission(mine.getBreakPermission()))) {
                    e.setCancelled(true);
                } else {
                    if (mine.getType() == RMine.Type.FARM && !FarmItem.getCrops().contains(block.getType())) {
                        e.setCancelled(true);
                    } else {
                        MineItem mi = mine.getMineItems().get(mine.getType() == RMine.Type.FARM ? FarmItem.getIconFromCrop(block.getType()) : block.getType());
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
        for (RMine mine : this.getMines().values()) {
            if (mine.getTimer() != null) {
                mine.getTimer().kill();
            }
        }
        this.clearMemory();
    }

    @Override
    public void setBounds(final RMine m, final Player p) {
        if (m.getType() == RMine.Type.SCHEMATIC) {
            return;
        }

        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final LocalSession localSession = w.getSession(p.getPlayer());
            //final Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            final Region r = localSession.getSelection(BukkitAdapter.adapt(p.getWorld()));

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                m.setPOS(pos1, pos2);
                m.fillContent();
                TranslatableLine.SYSTEM_REGION_UPDATED.send(p);
                m.reset();
                m.saveData(RMine.MineData.POS);

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
            if (mine.getTimer() != null) {
                mine.getTimer().kill();
            }
            if (mine.getSigns() != null || !mine.getSigns().isEmpty()) {
                mine.getSigns().forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
            }
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
        m.rename(ChatColor.stripColor(Text.color(newName)));
        this.registerMine(m);
    }

    @Override
    public void unregisterMine(final RMine m) {
        m.deleteConfig();
        this.getMines().remove(m.getName());
    }

    @Override
    public void registerMine(final RMine m) {
        addMine(m);
        m.saveData(RMine.MineData.ALL);
    }
}
