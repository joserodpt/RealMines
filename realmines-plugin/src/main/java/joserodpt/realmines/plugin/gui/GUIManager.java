package joserodpt.realmines.plugin.gui;

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

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.actions.MineActionCommand;
import joserodpt.realmines.api.mine.components.actions.MineActionDropItem;
import joserodpt.realmines.api.mine.components.actions.MineActionGiveItem;
import joserodpt.realmines.api.mine.components.actions.MineActionMoney;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.GUIBuilder;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.PickType;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final RealMines rm;

    public GUIManager(final RealMines rm) {
        this.rm = rm;
    }

    public static ItemStack makeMineIcon(final RMine m) {
        return Items.createItemLore(Material.TRIPWIRE_HOOK, 1, m.getMineColor().getColorPrefix() + " &6&l" + m.getDisplayName(), var(m));
    }

    private static List<String> var(final RMine m) {
        final List<String> ret = new ArrayList<>();
        List<String> config = RMLanguageConfig.file().getStringList("GUI.Items.Mine.Description");
        if (config.size() > 2) {
            config = config.subList(0, config.size() - 2);
        }
        config.forEach(s -> ret.add(Text.color(s.replaceAll("%remainingblocks%", String.valueOf(m.getRemainingBlocks())).replaceAll("%totalblocks%", String.valueOf(m.getBlockCount())).replaceAll("%bar%", m.getBar()))));
        return ret;
    }

    public void openMineChooserType(final Player target, final String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final GUIBuilder inventory = new GUIBuilder(TranslatableLine.GUI_CHOOSE_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).get(), 27, target.getUniqueId());

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createMine(target, name);
                        }, Items.createItem(Material.CHEST, 1, TranslatableLine.GUI_BLOCKS_NAME.get()),
                        11);

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createSchematicMine(target, name);
                        }, Items.createItem(Material.FILLED_MAP, 1, TranslatableLine.GUI_SCHEMATIC_NAME.get()),
                        13);

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createCropsMine(target, name);
                        }, Items.createItem(Material.WHEAT, 1, TranslatableLine.GUI_FARM_NAME.get()),
                        15);

                inventory.openInventory(target);
            }
        }.runTaskLater(this.rm.getPlugin(), 2);
    }

    public void openBreakActionChooser(final Player target, final RMine r, final MineItem mi) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final GUIBuilder inventory = new GUIBuilder(Text.color("New Action for: " + Text.beautifyMaterialName(mi.getMaterial())), 27, target.getUniqueId());

                inventory.addItem(e -> {
                            Text.send(target, "Input in the chat the amount to give:");
                            new PlayerInput(target, s -> {
                                final double d;
                                try {
                                    d = Double.parseDouble(s);
                                } catch (final Exception ex) {
                                    Text.send(target, "&cWhat you inserted is not a valid double.");
                                    return;
                                }

                                mi.getBreakActions().add(new MineActionMoney(mi.getNewBreakActionCode(r.getName(), mi.getMaterial().name()), r.getName(), 50D, d));
                                r.saveData(RMine.Data.BLOCKS);

                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            }, s -> {
                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            });

                        }, Items.createItem(Material.EMERALD, 1, "&b&lGive Money"),
                        10);

                inventory.addItem(e -> {

                            Text.send(target, "Input in the chat the chance for the break action (0-100%):");
                            new PlayerInput(target, s -> {
                                final double d;
                                try {
                                    d = Double.parseDouble(s);
                                } catch (final Exception ex) {
                                    Text.send(target, "&cWhat you inserted is not a valid double.");
                                    return;
                                }

                                if (target.getInventory().getItemInMainHand().getAmount() != 0 || target.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                    Text.send(target, "&cYou don't have an item in your main hand.");
                                    return;
                                }

                                mi.getBreakActions().add(new MineActionDropItem(mi.getNewBreakActionCode(r.getName(), mi.getMaterial().name()), r.getName(), d, target.getInventory().getItemInMainHand()));
                                r.saveData(RMine.Data.BLOCKS);

                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            }, s -> {
                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            });

                        }, Items.createItem(Material.DROPPER, 1, "&b&lDrop Item"),
                        12);

                inventory.addItem(e -> {

                            Text.send(target, "Input in the chat the chance for the break action (0-100%):");
                            new PlayerInput(target, s -> {
                                final double d;
                                try {
                                    d = Double.parseDouble(s);
                                } catch (final Exception ex) {
                                    Text.send(target, "&cWhat you inserted is not a valid double.");
                                    return;
                                }

                                mi.getBreakActions().add(new MineActionGiveItem(mi.getNewBreakActionCode(r.getName(), mi.getMaterial().name()), r.getName(), d, target.getInventory().getItemInMainHand()));
                                r.saveData(RMine.Data.BLOCKS);

                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            }, s -> {
                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            });

                        }, Items.createItem(Material.CHEST, 1, "&b&lGive Item"),
                        14);

                inventory.addItem(e -> {

                            Text.send(target, "Input in the chat the command for the break action to execute:");
                            new PlayerInput(target, s -> {
                                mi.getBreakActions().add(new MineActionCommand(mi.getNewBreakActionCode(r.getName(), mi.getMaterial().name()), r.getName(), 50D, s));
                                r.saveData(RMine.Data.BLOCKS);

                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            }, s -> {
                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            });

                        }, Items.createItem(Material.COMMAND_BLOCK, 1, "&b&lExecute Command"),
                        16);

                inventory.openInventory(target);
            }
        }.runTaskLater(this.rm.getPlugin(), 2);
    }

    public void openMine(final RMine m, final Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final GUIBuilder inventory = new GUIBuilder(Text.color(m.getMineColor().getColorPrefix() + " " + m.getDisplayName() + " &r" + m.getBar()), 27, target.getUniqueId(),
                        Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&f"));

                inventory.addItem(e -> {
                    target.closeInventory();
                    new PlayerInput(target, s -> {
                        rm.getMineManager().renameMine(m, s);
                        TranslatableLine.SYSTEM_MINE_RENAMED.setV1(TranslatableLine.ReplacableVar.NAME.eq(s)).send(target);
                        openMine(m, target);
                    }, s -> rm.getGUIManager().openMine(m, target));
                }, Items.createItemLore(Material.FILLED_MAP, 1, TranslatableLine.GUI_NAME_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Name.Description")), 0);

                inventory.addItem(e -> {
                            target.closeInventory();
                            Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                                final MineItensGUI v = new MineItensGUI(rm, target, m);
                                v.openInventory(target);
                            }, 2);
                        }, Items.createItemLore(Material.CHEST, 1, TranslatableLine.GUI_MINE_BLOCKS_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Blocks.Description")),
                        10);

                inventory.addItem(e -> {
                            target.closeInventory();
                            Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                                final MineResetGUI mrm = new MineResetGUI(rm, target, m);
                                mrm.openInventory(target);
                            }, 2);
                        }, Items.createItemLore(Material.ANVIL, 1, TranslatableLine.GUI_RESETS_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Resets.Description")),
                        12);
                inventory.addItem(e -> {
                    target.closeInventory();
                    rm.getMineManager().teleport(target, m, m.isSilent(), false);
                }, Items.createItemLore(Material.ENDER_PEARL, 1, TranslatableLine.GUI_TELEPORT_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Teleport.Description")), 20);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                        final BlockPickerGUI s = new BlockPickerGUI(rm, m, target, PickType.ICON, "");
                        s.openInventory(target);
                    }, 2);
                }, Items.createItemLore(m.getIcon(), 1, TranslatableLine.GUI_ICON_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Icon.Description")), 2);

                inventory.addItem(e -> {
                    target.closeInventory();
                    new PlayerInput(target, s -> {
                        m.setDisplayName(s);
                        rm.getGUIManager().openMine(m, target);
                    }, s -> rm.getGUIManager().openMine(m, target));
                }, Items.createItemLore(Material.PAPER, 1, TranslatableLine.GUI_DISPLAYNAME_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Displayname.Description")), 4);

                inventory.addItem(e -> {
                    m.clear();
                    TranslatableLine.SYSTEM_MINE_CLEAR.send(target);
                }, Items.createItemLore(Material.TNT, 1, TranslatableLine.GUI_CLEAR_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Clear.Description")), 22);

                inventory.addItem(e -> m.reset(), Items.createItemLore(Material.DROPPER, 1, TranslatableLine.GUI_RESET_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Reset.Description")), 14);

                inventory.addItem(e -> m.setHighlight(!m.isHighlighted()), Items.createItemLore(Material.REDSTONE_TORCH, 1, TranslatableLine.GUI_BOUNDARIES_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Boundaries.Description")), 6);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                        final MineColorPickerGUI mcp = new MineColorPickerGUI(rm, target, m);
                        mcp.openInventory(target);
                    }, 2);
                }, m.getMineColor().getItem(TranslatableLine.GUI_MINE_COLOR_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.MineColor.Description")), 24);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                        final MineFacesGUI m1 = new MineFacesGUI(rm, target, m);
                        m1.openInventory(target);
                    }, 2);
                }, Items.createItemLore(Material.SCAFFOLDING, 1, TranslatableLine.GUI_FACES_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Faces.Description")), 16);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                        final MineListGUI m1 = new MineListGUI(rm, target, MineListGUI.MineListSort.DEFAULT);
                        m1.openInventory(target);
                    }, 2);
                }, Items.createItemLore(Material.RED_BED, 1, TranslatableLine.GUI_GO_BACK_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.Back.Description")), 26);

                inventory.addItem(event -> {
                }, makeMineIcon(m), 13);

                inventory.openInventory(target);
            }
        }.runTaskLater(this.rm.getPlugin(), 2);
    }
}
