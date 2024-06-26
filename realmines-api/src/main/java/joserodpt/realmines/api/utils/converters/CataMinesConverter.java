package joserodpt.realmines.api.utils.converters;

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

import de.c4t4lysm.catamines.schedulers.MineManager;
import de.c4t4lysm.catamines.utils.mine.mines.CuboidCataMine;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;

public class CataMinesConverter implements RMConverterBase {
    private final RealMinesAPI rm;

    public CataMinesConverter(final RealMinesAPI rm) {
        this.rm = rm;
    }

    @Override
    public RMSupportedConverters getPlugin() {
        return RMSupportedConverters.CATA_MINES;
    }

    @Override
    public void convert(CommandSender cmd) {
        if (!Bukkit.getPluginManager().isPluginEnabled(this.getPlugin().getSourceName())) {
            Text.send(cmd, "&cCataMines is not enabled. &fTerminating import process.");
            return;
        }

        cmd.sendMessage(Text.color("&7----------------- &9Real&bMines &f&lImport &7-----------------"));
        Text.send(cmd, "&aImporting Mines from: &b" + this.getPlugin().getSourceName());

        for (CuboidCataMine cataMine : MineManager.getInstance().getMines()) {
            Text.send(cmd, "&aImporting now &b" + cataMine.getName());

            if (rm.getMineManager().getMines().containsKey(cataMine.getName())) {
                Text.send(cmd, "&cThere is already a mine named " + cataMine.getName() + ". &fSkipping!");
                continue;
            }

            Text.send(cmd, " &f> Mine has &b" + cataMine.getBlocks().size() + "&f blocks.");

            final BlockMine m = new BlockMine(Bukkit.getWorld(cataMine.getWorld()), ChatColor.stripColor(Text.color(cataMine.getName())), cataMine.getName(), new HashMap<>(), new ArrayList<>(), WorldEditUtils.toLocation(cataMine.getRegion().getMinimumPoint(), Bukkit.getWorld(cataMine.getWorld())), WorldEditUtils.toLocation(cataMine.getRegion().getMaximumPoint(), Bukkit.getWorld(cataMine.getWorld())),
                    Material.COBBLESTONE, null, false, true, 20, 60, MineColor.WHITE, new HashMap<>(), false, false, rm.getMineManager());

            cataMine.getBlocks().forEach(cataMineBlock ->
                    m.addItem(new MineBlockItem(cataMineBlock.getBlockData().getMaterial(), cataMineBlock.getChance() / 100)));

            final double value2 = cataMine.getResetPercentage();

            if ((int) value2 != -1) {
                m.setReset(RMine.Reset.PERCENTAGE, true);
                m.setReset(RMine.Reset.PERCENTAGE, (int) (value2 * 100.0));
                Text.send(cmd, " &f> Importing reset percentage of: &b" + (value2 * 100.0) + "%");
            }

            int value = cataMine.getResetDelay();
            if (value > 5) {
                m.setReset(RMine.Reset.TIME, true);
                m.setReset(RMine.Reset.TIME, value);
                Text.send(cmd, " &f> Importing reset delay of: &b" + value + " seconds");
            } else {
                m.setReset(RMine.Reset.TIME, false);
            }

            m.reset(RMine.ResetCause.CREATION);

            if (cataMine.getTeleportLocation().getY() >= 0) {
                Text.send(cmd, " &f> Importing mine teleport position.");
                m.setTeleport(cataMine.getTeleportLocation());
                m.saveData(RMine.Data.TELEPORT);
            }

            m.saveAll();

            rm.getMineManager().addMine(m);
            Text.send(cmd, "&aSucessfully imported mine " + m.getDisplayName());
        }
    }
}
