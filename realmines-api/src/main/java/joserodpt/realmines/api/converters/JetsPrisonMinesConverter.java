package joserodpt.realmines.api.converters;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class JetsPrisonMinesConverter implements RMConverterBase {
    private final RealMinesAPI rm;

    public JetsPrisonMinesConverter(final RealMinesAPI rm) {
        this.rm = rm;
    }

    @Override
    public RMSupportedConverters getPlugin() {
        return RMSupportedConverters.JETS_PRISON_MINES;
    }

    @Override
    public void convert(CommandSender cmd) {
        cmd.sendMessage(Text.color("&7----------------- &9Real&bMines &f&lImport &7-----------------"));
        Text.send(cmd, "&aImporting Mines from: &b" + this.getPlugin().getSourceName());

        final File JetsPrisonMinesPath = new File(rm.getPlugin().getDataFolder().getParent() + "//JetsPrisonMines//mines");

        if (!JetsPrisonMinesPath.exists() || !JetsPrisonMinesPath.isDirectory()) {
            Text.send(cmd, "&cError: The path for JetsPrisonMines' mines does not exist: " + JetsPrisonMinesPath.getAbsolutePath());
            return;
        }

        for (File file : Objects.requireNonNull(JetsPrisonMinesPath.listFiles())) {
            try {
                YamlDocument mineFile = YamlDocument.create(file);

                String mineName = mineFile.getString("mine_name");
                Text.send(cmd, "&aImporting now &b" + mineName);

                if (rm.getMineManager().getMines().containsKey(mineName)) {
                    Text.send(cmd, "&cThere is already a mine named " + mineName + ". &fSkipping!");
                    continue;
                }

                List<String> blocks = mineFile.getStringList("blocks");

                World w = Bukkit.getWorld(Objects.requireNonNull(mineFile.getString("region.world")));
                if (w == null) {
                    Text.send(cmd, "&cError: Could not find world: " + mineFile.getString("region.world") + " for mine: " + mineName + ". &fSkipping!");
                    continue;
                }

                double xMin = mineFile.getInt("region.xmin");
                double yMin = mineFile.getInt("region.ymin");
                double zMin = mineFile.getInt("region.zmin");

                Location min = new Location(w, xMin, yMin, zMin);

                double xMax = mineFile.getInt("region.xmax");
                double yMax = mineFile.getInt("region.ymax");
                double zMax = mineFile.getInt("region.zmax");

                Location max = new Location(w, xMax, yMax, zMax);

                final BlockMine m = new BlockMine(ChatColor.stripColor(Text.color(mineName)),
                        w,
                        min,
                        max);

                m.setIcon(Material.COBBLESTONE);

                Section sec = (Section) mineFile.get("teleport_location");
                if (sec != null) {
                    double spawnX = sec.getDouble("x");
                    double spawnY = sec.getDouble("y");
                    double spawnZ = sec.getDouble("z");
                    double spawnPitch = sec.getDouble("pitch");
                    double spawnYaw = sec.getDouble("yaw");

                    m.setTeleport(new Location(w, spawnX, spawnY, spawnZ, (float) spawnYaw, (float) spawnPitch));
                }

                boolean rUseTimer = mineFile.getBoolean("reset.use_timer");
                int rTimer = mineFile.getInt("reset.timer");

                if (rUseTimer) {
                    m.setResetState(RMine.Reset.TIME, true);
                    m.setResetValue(RMine.Reset.TIME, rTimer);
                    Text.send(cmd, " &f> Importing reset delay of: &b" + rTimer + " seconds");
                } else {
                    m.setResetState(RMine.Reset.TIME, false);
                }

                boolean rUsePercentage = mineFile.getBoolean("reset.use_percentage");
                double rPercentage = mineFile.getDouble("reset.percentage");

                if (rUsePercentage) {
                    m.setResetState(RMine.Reset.PERCENTAGE, true);
                    m.setResetValue(RMine.Reset.PERCENTAGE, (int) (rPercentage / 100.0));
                    Text.send(cmd, " &f> Importing reset percentage of: &b" + (rPercentage / 100.0) + "%");
                }

                boolean rUseMessages = mineFile.getBoolean("reset.use_messages");
                m.setSilent(!rUseMessages);

                for (String blockDetails : blocks) {
                    String[] bd = blockDetails.split(":");

                    String blockName = bd.length > 0 ? bd[0] : null;
                    String chanceStr = bd.length > 1 ? bd[1] : "1.0";

                    Material mat;
                    try {
                        mat = Material.valueOf(blockName);

                        m.addItem("default", new MineBlockItem(mat, Double.parseDouble(chanceStr) / 100));
                    } catch (IllegalArgumentException e) {
                        Text.send(cmd, "&cError: Could not find block: " + blockName + " for mine: " + mineName + ". &fSkipping!");
                    }
                }

                m.reset(RMine.ResetCause.IMPORT);

                rm.getMineManager().addMine(m);
                Text.send(cmd, "&aSucessfully imported mine " + m.getDisplayName());

            } catch (IllegalArgumentException e) {
                Text.send(cmd, "&cError: This mine has a location that has an unknown world. &fSkipping!");
                e.printStackTrace();
            } catch (Exception e) {
                Text.send(cmd, "&cError: Could not load JetsPrisonMines file: " + file.getName() + "! See console for more details.");
                rm.getPlugin().getLogger().severe("Error loading JetsPrisonMines file: " + file.getName());
                e.printStackTrace();
            }
        }

        //end
        Text.send(cmd, "&aEnded Mine Import Process from &b" + this.getPlugin().getSourceName());
        cmd.sendMessage(Text.color("&7----------------- &9Real&bMines &f&lImport &7-----------------"));
    }
}
