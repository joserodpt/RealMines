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
import joserodpt.realmines.api.managers.PrivateMinesWorld;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.managers.PrivateMinesManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockEvents implements Listener {

    private final RealMines rm;

    public BlockEvents(final RealMines rm) {
        this.rm = rm;
    }

    /**
     * Last time each player was told they can't build here, so a held down mouse button doesn't fill
     * their chat with the same line.
     */
    private final Map<UUID, Long> refusedAt = new HashMap<>();
    private static final long REFUSED_COOLDOWN_MS = 3000L;

    /**
     * Whether a block in the private mines world is out of bounds for this player.
     * <p>
     * That world is RealMines' own: the only blocks anybody has business touching in it are the ones
     * inside a mine, and which of those is then the mine's own business - {@code findBlockUpdate} asks
     * whether they own it or are trusted on it. Everything else is plugin built: the walkway, its fence,
     * the floor under the pit and the empty space between slots.
     */
    private boolean outsideAnyMine(final Player p, final Block block) {
        return !p.hasPermission(PrivateMinesManager.ADMIN_PERMISSION) && this.outsidePrivateMines(block);
    }

    /**
     * Whether a block is in the private mines world but not inside any mine, whoever or whatever is
     * acting on it.
     */
    private boolean outsidePrivateMines(final Block block) {
        return block.getWorld().getName().equals(PrivateMinesWorld.NAME)
                && rm.getMineManager().getMineWithBlock(block) == null;
    }

    private void refuse(final Player p) {
        final long now = System.currentTimeMillis();
        final Long last = this.refusedAt.get(p.getUniqueId());
        if (last != null && now - last < REFUSED_COOLDOWN_MS) {
            return;
        }
        //dropped once stale, so this never grows with the number of players ever refused
        this.refusedAt.values().removeIf(stamp -> now - stamp >= REFUSED_COOLDOWN_MS);
        this.refusedAt.put(p.getUniqueId(), now);
        TranslatableLine.PRIVATE_MINE_CANT_BUILD.send(p);
    }

    //LOW, so a block that is out of bounds is refused before the mine handling below looks at it
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldBreak(final BlockBreakEvent e) {
        if (this.outsideAnyMine(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
            this.refuse(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldPlace(final BlockPlaceEvent e) {
        if (this.outsideAnyMine(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
            this.refuse(e.getPlayer());
        }
    }

    //the rest of the ways to change the private world without placing or breaking a block. Each is limited
    //to the player's own slot anyway, but the walkway and fence around it are plugin built and must stay

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldBucketEmpty(final PlayerBucketEmptyEvent e) {
        if (this.outsideAnyMine(e.getPlayer(), e.getBlockClicked().getRelative(e.getBlockFace()))) {
            e.setCancelled(true);
            this.refuse(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldBucketFill(final PlayerBucketFillEvent e) {
        if (this.outsideAnyMine(e.getPlayer(), e.getBlockClicked())) {
            e.setCancelled(true);
            this.refuse(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldPistonExtend(final BlockPistonExtendEvent e) {
        for (final Block block : e.getBlocks()) {
            //pushing a walkway block, or pushing a mine block out onto the walkway
            if (this.outsidePrivateMines(block) || this.outsidePrivateMines(block.getRelative(e.getDirection()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldPistonRetract(final BlockPistonRetractEvent e) {
        //a sticky piston in the mine pulling a walkway block in, where it could then be mined
        if (e.getBlocks().stream().anyMatch(this::outsidePrivateMines)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrivateWorldFlow(final BlockFromToEvent e) {
        //water or lava poured in a mine spreading out over the walkway
        if (this.outsidePrivateMines(e.getToBlock())) {
            e.setCancelled(true);
        }
    }

    //a player placed block keeps that status when a piston moves it, or pushing it one block over would
    //be enough to get its break actions paid out
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent e) {
        this.movePlacedBlocks(e.getBlocks(), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        this.movePlacedBlocks(e.getBlocks(), e.getDirection());
    }

    private void movePlacedBlocks(final List<Block> blocks, final BlockFace direction) {
        //every source is forgotten before any destination is remembered, since in a row of pushed blocks
        //one block's destination is the next one's source
        final List<Location> destinations = new ArrayList<>();
        for (final Block block : blocks) {
            final RMine mine = rm.getMineManager().getMineWithBlock(block);
            if (mine != null && mine.forgetPlacedBlock(block.getLocation())) {
                destinations.add(block.getRelative(direction).getLocation());
            }
        }
        for (final Location destination : destinations) {
            final RMine mine = rm.getMineManager().getMineWithBlock(destination.getBlock());
            if (mine != null) {
                mine.rememberPlacedBlock(destination);
            }
        }
    }

    //a crop replanted in a farm mine pays nothing when broken straight away, but it has earned its break
    //actions once it has grown all the way by itself
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropGrow(final BlockGrowEvent e) {
        if (e.getNewState().getBlockData() instanceof final Ageable crop && crop.getAge() >= crop.getMaximumAge()) {
            final RMine mine = rm.getMineManager().getMineWithBlock(e.getBlock());
            if (mine != null && mine.getType() == RMine.Type.FARM) {
                mine.forgetPlacedBlock(e.getBlock().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        final MineItem mi = rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getBlock(), true);
        if (mi != null && mi.areVanillaDropsDisabled()) {
            e.setDropItems(false);
        }
    }

    //MONITOR, once nothing can cancel the break any more: handing the drops over earlier let a plugin that
    //cancelled it afterwards leave the player with both the drops and the block
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBroken(final BlockBreakEvent e) {
        //isDropItems is already false when the mine item disabled vanilla drops
        if (e.isDropItems() && RMConfig.file().getBoolean("RealMines.sendMinedItemsToInventory")
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

    //ignoreCancelled on these three: a place, trample or blast another plugin (WorldGuard, say) cancelled
    //never changed the mine, so it must not move the mined count or run break actions either
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getBlock(), false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFarmStep(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
            rm.getMineManager().findBlockUpdate(e.getPlayer(), e, e.getClickedBlock().getRelative(BlockFace.UP), true);
        }
    }

    @EventHandler(ignoreCancelled = true) //for creeper and TNT explosions
    public void onEntityExplode(final EntityExplodeEvent e) {
        this.handleExplosion(e.blockList(), e);
    }

    @EventHandler(ignoreCancelled = true) //for beds and respawn anchors, which explode as blocks
    public void onBlockExplode(final BlockExplodeEvent e) {
        this.handleExplosion(e.blockList(), e);
    }

    private void handleExplosion(final List<Block> blocks, final Cancellable e) {
        //one blast reports every block through the same event, so blocks that have to survive it are
        //dropped from the list rather than cancelling the explosion for everyone
        blocks.removeIf(block -> {
            final RMine mine = rm.getMineManager().getMineWithBlock(block);

            //in RealMines' own world a blast may take a mine's blocks and nothing else. The fence and the
            //floor under the pit are barriers, which no explosion touches anyway; the walkway is not
            if (block.getWorld().getName().equals(PrivateMinesWorld.NAME)) {
                return mine == null;
            }

            //anywhere else, a private mine is somebody's property that a stray blast must not reach
            return mine != null && mine.isPrivate();
        });

        blocks.forEach(block -> rm.getMineManager().findBlockUpdate(null, e, block, true));
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
