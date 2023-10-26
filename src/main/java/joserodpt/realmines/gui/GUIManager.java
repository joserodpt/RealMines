package joserodpt.realmines.gui;

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
import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.components.actions.MineActionCommand;
import joserodpt.realmines.mine.components.actions.MineActionDropItem;
import joserodpt.realmines.mine.components.actions.MineActionGiveItem;
import joserodpt.realmines.mine.components.actions.MineActionMoney;
import joserodpt.realmines.mine.components.items.MineItem;
import joserodpt.realmines.mine.types.BlockMine;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.util.GUIBuilder;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.PlayerInput;
import joserodpt.realmines.util.Text;
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
        return Items.createItemLore(Material.TRIPWIRE_HOOK, 1, m.getMineColor().getColorPrefix()+ " &6&l" + m.getDisplayName(), var(m));
    }

    private static List<String> var(final RMine m) {
        final List<String> ret = new ArrayList<>();
        final List<String> config = Language.file().getStringList("GUI.Items.Mine.Description");
        config.remove(config.size() - 1);
        config.forEach(s -> ret.add(Text.color(s.replaceAll("%remainingblocks%", String.valueOf(m.getRemainingBlocks())).replaceAll("%totalblocks%", String.valueOf(m.getBlockCount())).replaceAll("%bar%", m.getBar()))));
        return ret;
    }

    public void openMineChooserType(final Player target, final String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final GUIBuilder inventory = new GUIBuilder(Text.color(Language.file().getString("GUI.Choose-Name").replaceAll("%mine%", name)), 27, target.getUniqueId());

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createMine(target, name);
                        }, Items.createItem(Material.CHEST, 1, Language.file().getString("GUI.Items.Blocks.Name")),
                        11);

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createSchematicMine(target, name);
                        }, Items.createItem(Material.FILLED_MAP, 1, Language.file().getString("GUI.Items.Schematic.Name")),
                        13);

                inventory.addItem(e -> {
                            target.closeInventory();
                            rm.getMineManager().createCropsMine(target, name);
                        }, Items.createItem(Material.WHEAT, 1, Language.file().getString("GUI.Items.Farm.Name")),
                        15);

                inventory.openInventory(target);
            }
        }.runTaskLater(this.rm, 2);
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

                                mi.getBreakActions().add(new MineActionMoney(mi.getNewBrkActCode(r.getName(), mi.getMaterial().name()), 50D, d));
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

                                if (target.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                    Text.send(target, "&cYou don't have an item in your main hand.");
                                    return;
                                }

                                mi.getBreakActions().add(new MineActionDropItem(mi.getNewBrkActCode(r.getName(), mi.getMaterial().name()), d, target.getInventory().getItemInMainHand()));
                                r.saveData(RMine.Data.BLOCKS);

                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            }, s -> {
                                final MineBreakActionsGUI v = new MineBreakActionsGUI(rm, target, r, mi);
                                v.openInventory(target);
                            });

                        }, Items.createItem(Material.DROPPER, 1,"&b&lDrop Item"),
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

                                mi.getBreakActions().add(new MineActionGiveItem(mi.getNewBrkActCode(r.getName(), mi.getMaterial().name()), d, target.getInventory().getItemInMainHand()));
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
                                mi.getBreakActions().add(new MineActionCommand(mi.getNewBrkActCode(r.getName(), mi.getMaterial().name()), 50D, s));
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
        }.runTaskLater(this.rm, 2);
    }

    public void openMine(final RMine m, final Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final GUIBuilder inventory = new GUIBuilder(Text.color(m.getMineColor().getColorPrefix() + " " + m.getDisplayName() + " &r" + m.getBar()), 27, target.getUniqueId(),
                        Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&f"));

                if (m.getType() == RMine.Type.BLOCKS || m.getType() == RMine.Type.FARM) {
                    inventory.addItem(e -> {
                                target.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                                    final MineItensGUI v = new MineItensGUI(GUIManager.this.rm, target, m);
                                    v.openInventory(target);
                                }, 2);
                            }, Items.createItemLore(Material.CHEST, 1, Language.file().getString("GUI.Items.Blocks.Name"), Language.file().getStringList("GUI.Items.Blocks.Description")),
                            10);
                }

                inventory.addItem(e -> {
                            target.closeInventory();
                            Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                                final MineResetGUI mrm = new MineResetGUI(GUIManager.this.rm, target, m);
                                mrm.openInventory(target);
                            }, 2);
                        }, Items.createItemLore(Material.ANVIL, 1, Language.file().getString("GUI.Items.Resets.Name"), Language.file().getStringList("GUI.Items.Resets.Description")),
                        12);
                inventory.addItem(e -> {
                    target.closeInventory();
                    GUIManager.this.rm.getMineManager().teleport(target, m, m.isSilent());
                }, Items.createItemLore(Material.ENDER_PEARL, 1, Language.file().getString("GUI.Items.Teleport.Name"), Language.file().getStringList("GUI.Items.Teleport.Description")), 20);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                        final BlockPickerGUI s = new BlockPickerGUI(GUIManager.this.rm, m, target, BlockPickerGUI.PickType.ICON, "");
                        s.openInventory(target);
                    }, 2);
                }, Items.createItemLore(m.getIcon(), 1, Language.file().getString("GUI.Items.Icon.Name"), Language.file().getStringList("GUI.Items.Icon.Description")), 2);

                inventory.addItem(e -> {
                    target.closeInventory();
                    new PlayerInput(target, s -> {
                        m.setDisplayName(s);
                        GUIManager.this.rm.getGUIManager().openMine(m, target);
                    }, s -> GUIManager.this.rm.getGUIManager().openMine(m, target));
                }, Items.createItemLore(Material.PAPER, 1, Language.file().getString("GUI.Items.Name.Name"), Language.file().getStringList("GUI.Items.Name.Description")), 4);

                inventory.addItem(e -> {
                    m.clear();
                    Text.send(target, Language.file().getString("System.Mine-Clear"));
                }, Items.createItemLore(Material.TNT, 1, Language.file().getString("GUI.Items.Clear.Name"), Language.file().getStringList("GUI.Items.Clear.Description")), 22);

                inventory.addItem(e -> m.reset(), Items.createItemLore(Material.DROPPER, 1, Language.file().getString("GUI.Items.Reset.Name"), Language.file().getStringList("GUI.Items.Reset.Description")), 14);

                inventory.addItem(e -> m.setHighlight(!m.isHighlighted()), Items.createItemLore(Material.REDSTONE_TORCH, 1, Language.file().getString("GUI.Items.Boundaries.Name"), Language.file().getStringList("GUI.Items.Boundaries.Description")), 6);

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                        final MineColorPickerGUI mcp = new MineColorPickerGUI(GUIManager.this.rm, target, m);
                        mcp.openInventory(target);
                    }, 2);
                }, m.getMineColor().getItem(Language.file().getString("GUI.Items.MineColor.Name"), Language.file().getStringList("GUI.Items.MineColor.Description")), 24);

                if (m instanceof BlockMine) {
                    inventory.addItem(e -> {
                        target.closeInventory();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                            final MineFacesGUI m1 = new MineFacesGUI(GUIManager.this.rm, target, m);
                            m1.openInventory(target);
                        }, 2);
                    }, Items.createItemLore(Material.SCAFFOLDING, 1, Language.file().getString("GUI.Items.Faces.Name"), Language.file().getStringList("GUI.Items.Faces.Description")), 16);
                }

                inventory.addItem(e -> {
                    target.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(GUIManager.this.rm, () -> {
                        final MineListGUI m1 = new MineListGUI(GUIManager.this.rm, target);
                        m1.openInventory(target);
                    }, 2);
                }, Items.createItemLore(Material.RED_BED, 1, Language.file().getString("GUI.Items.Back.Name"), Language.file().getStringList("GUI.Items.Back.Description")), 26);

                inventory.addItem(event -> {
                }, makeMineIcon(m), 13);

                inventory.openInventory(target);
            }
        }.runTaskLater(this.rm, 2);
    }
}
