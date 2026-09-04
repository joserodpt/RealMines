package joserodpt.realmines.plugin.events;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import com.google.common.collect.ImmutableSet;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.event.RealMinesBlockBreakEvent;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

public class BlockEvents implements Listener {

    private final RealMines rm;

    public BlockEvents(final RealMines rm) {
        this.rm = rm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent e) {
        final MineItem mi = rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getBlock(), true);
        if (mi != null && mi.areVanillaDropsDisabled()) {
            e.setDropItems(false);
            return;
        }

        if (!e.isCancelled() && RMConfig.file().getBoolean("RealMines.sendMinedItemsToInventory")
                && rm.getMineManager().getMineWithBlock(e.getBlock()) != null) {
            sendDropsToInventory(e.getPlayer(), e);
        }
    }

    private void sendDropsToInventory(final Player p, final BlockBreakEvent e) {
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        final Block block = e.getBlock();
        final Collection<ItemStack> drops = block.getDrops(p.getInventory().getItemInMainHand());
        if (drops.isEmpty()) {
            return;
        }

        //vanilla drops are replaced by the ones given to the player
        e.setDropItems(false);

        final Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);
        for (final ItemStack drop : drops) {
            //whatever doesn't fit in the player's inventory is dropped on the ground
            p.getInventory().addItem(drop).values().forEach(leftover -> block.getWorld().dropItemNaturally(loc, leftover));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent e) {
        rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getBlock(), false);
    }

    @EventHandler
    public void onFarmStep(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
            rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getClickedBlock().getRelative(BlockFace.UP), true);
        }
    }

    @EventHandler //for creeper explosions
    public void onEntityExplode(final EntityExplodeEvent e) {
        //one blast reports every block through the same event, so a block inside somebody's private mine
        //has to be dropped from the list rather than cancelling the explosion for everyone
        e.blockList().removeIf(block -> {
            final RMine mine = rm.getMineManager().getMineWithBlock(block);
            return mine != null && mine.isPrivate();
        });

        e.blockList().forEach(block -> rm.getMineManager().findBlockUpdate(null, e, block, true));
    }

    @EventHandler
    public void mineBlockBreak(final RealMinesBlockBreakEvent e) {
        e.getMine().processBlockBreakEvent(e, true);
    }

    private final Set<String> signset = ImmutableSet.of("pm", "pl", "bm", "br", "b", "pb", "tl", "sl");

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if (event.getLine(0).contains("[realmines]")
                || event.getLine(0).contains("[rm]")
                || event.getLine(0).contains("[RealMines]")) {
            event.setLine(0, Text.getPrefix());
            final String name = event.getLine(1);

            final RMine m = rm.getMineManager().getMine(name);

            if (m != null) {
                final String modif = event.getLine(2);
                assert modif != null;
                if (signset.contains(modif.toLowerCase())) {
                    m.addSign(event.getBlock(), modif);
                    m.updateSigns();
                } else {
                    final String[] line = TranslatableLine.SIGNS_SETTING_NOT_FOUND.get().split("\\|");
                    event.setLine(1, Text.color(line[0]));
                    event.setLine(2, Text.color(line[1]));
                    event.setLine(3, Text.color(line[2]));
                }
            } else {
                final String[] line = TranslatableLine.SIGNS_MINE_NOT_FOUND.get().split("\\|");
                event.setLine(1, Text.color(line[0]));
                event.setLine(2, Text.color(line[1]));
                event.setLine(3, Text.color(line[2]));
            }
        }
    }
}
