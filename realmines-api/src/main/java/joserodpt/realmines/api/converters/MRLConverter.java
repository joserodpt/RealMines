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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MRLConverter implements RMConverterBase {
    private final RealMinesAPI rm;

    public MRLConverter(final RealMinesAPI rm) {
        this.rm = rm;
    }

    @Override
    public RMSupportedConverters getPlugin() {
        return RMSupportedConverters.MINE_RESET_LITE;
    }

    @Override
    public void convert(CommandSender cmd) {
        cmd.sendMessage(Text.color("&7----------------- &9Real&bMines &f&lImport &7-----------------"));
        Text.send(cmd, "&aImporting Mines from: &b" + this.getPlugin().getSourceName());

        final File MRLDirectory = new File(rm.getPlugin().getDataFolder().getParent() + "//MineResetLite//mines");
        if (!MRLDirectory.exists() || !MRLDirectory.isDirectory()) {
            Text.send(cmd, "&cError: The path for MineResetLite mines does not exist: " + MRLDirectory.getAbsolutePath());
            return;
        }

        ConfigurationSerialization.registerClass(com.koletar.jj.mineresetlite.Mine.class);

        File[] MRLyamlFiles = MRLDirectory.listFiles();
        for (int mineCounter = MRLyamlFiles.length, i = 0; i < mineCounter; ++i) {
            final com.koletar.jj.mineresetlite.Mine MRLmine = (com.koletar.jj.mineresetlite.Mine) YamlConfiguration.loadConfiguration(MRLyamlFiles[i]).get("mine");

            Text.send(cmd, "&aImporting now &b" + MRLmine.getName());

            if (rm.getMineManager().getMines().containsKey(MRLmine.getName())) {
                Text.send(cmd, "&cThere is already a mine named " + MRLmine.getName() + ". &fSkipping!");
                continue;
            }

            final Map<com.koletar.jj.mineresetlite.SerializableBlock, Double> composition = MRLmine.getComposition();
            final Map<Material, Double> blocks = new HashMap<>();
            for (final com.koletar.jj.mineresetlite.SerializableBlock serializableBlock : composition.keySet()) {
                blocks.put(Material.valueOf(serializableBlock.getBlockId()), composition.get(serializableBlock) * 100.0);
            }

            Text.send(cmd, " &f> Mine has &b" + blocks.size() + "&f blocks.");

            try {
                final BlockMine m = new BlockMine(ChatColor.stripColor(Text.color(MRLmine.getName())),
                        MRLmine.getWorld(),
                        MRLmine.getMin(), MRLmine.getMax());

                m.setIcon(Material.COBBLESTONE);

                blocks.forEach((material, aDouble) -> m.addItem("default", new MineBlockItem(material, aDouble / 100)));

                final double value2 = MRLmine.getResetPercent();
                if ((int) value2 != -1) {
                    m.setResetState(RMine.Reset.PERCENTAGE, true);
                    m.setResetValue(RMine.Reset.PERCENTAGE, (int) (value2 * 100.0));
                    Text.send(cmd, " &f> Importing reset percentage of: &b" + (value2 * 100.0) + "%");
                }

                m.reset(RMine.ResetCause.IMPORT);

                if (MRLmine.getTpY() >= 0) {
                    Text.send(cmd, " &f> Importing mine teleport position.");
                    m.setTeleport(MRLmine.getTpPos());
                }

                rm.getMineManager().addMine(m);
                Text.send(cmd, "&aSucessfully imported mine " + m.getDisplayName());
            } catch (RMFailedToLoadException e) {
                Text.send(cmd, "&cFailed to import mine " + e.getMineName() + " because: " + e.getReason());
            }
        }
        //end
        Text.send(cmd, "&aEnded Mine Import Process from &b" + this.getPlugin().getSourceName());
        cmd.sendMessage(Text.color("&7----------------- &9Real&bMines &f&lImport &7-----------------"));
    }
}
