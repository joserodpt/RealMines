package joserodpt.realmines.event;

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

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.util.Text;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockEvents implements Listener {

    private final RealMines rm;
    private final String noSetting = Language.file().getString("Signs.Setting-Not-Found");
    private final String noMine = Language.file().getString("Signs.Mine-Not-Found");

    public BlockEvents(final RealMines rm) {
        this.rm = rm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent e) {
        rm.getMineManager().findBlockUpdate(e, e.getBlock(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent e) {
        rm.getMineManager().findBlockUpdate(e, e.getBlock(), false);
    }

    @EventHandler //for creeper explosions
    public void onEntityExplode(final EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            rm.getMineManager().findBlockUpdate(e, block, true);
        }
    }

    @EventHandler
    public void mineBlockBreak(final MineBlockBreakEvent e) {
        e.getMine().processBlockBreakEvent(e.isBroken(), true);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if (event.getLine(0).contains("[realmines]") || event.getLine(0).contains("[RealMines]")) {
            event.setLine(0, Text.color(Config.file().getString("RealMines.Prefix")));
            final String name = event.getLine(1);

            final RMine m = rm.getMineManager().getMine(name);

            if (m != null) {
                final String modif = event.getLine(2);
                assert modif != null;
                if (rm.getMineManager().signset.contains(modif.toLowerCase())) {
                    m.addSign(event.getBlock(), modif);
                    m.updateSigns();
                } else {
                    final String[] line = this.noSetting.split("\\|");
                    event.setLine(1, Text.color(line[0]));
                    event.setLine(2, Text.color(line[1]));
                    event.setLine(3, Text.color(line[2]));
                }
            } else {
                final String[] line = this.noMine.split("\\|");
                event.setLine(1, Text.color(line[0]));
                event.setLine(2, Text.color(line[1]));
                event.setLine(3, Text.color(line[2]));
            }
        }
    }
}
