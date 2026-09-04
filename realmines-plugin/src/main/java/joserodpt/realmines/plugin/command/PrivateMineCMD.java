package joserodpt.realmines.plugin.command;

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

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Join;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.config.TranslatableLine.ReplacableVar;
import joserodpt.realmines.api.managers.PrivateMinePlatform;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.managers.PrivateMinesManagerAPI.ClaimResult;
import joserodpt.realmines.api.managers.PrivateMinesWorld;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.gui.PrivateMinesGUI;
import joserodpt.realmines.plugin.managers.PrivateMinesManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Command(value = "privatemine", alias = {"realminesprivate", "pmine"})
public class PrivateMineCMD extends BaseCommandWA {

    private final RealMines rm;

    public PrivateMineCMD(final RealMines rm) {
        this.rm = rm;
    }

    /**
     * Turns seconds into something readable, or the "never" wording when there is no expiry.
     */
    public static String formatTime(final long seconds) {
        if (seconds < 0) {
            return TranslatableLine.PRIVATE_MINE_NEVER_EXPIRES.get();
        }
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        }
        if (seconds < 86400) {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        }
        return (seconds / 86400) + "d " + ((seconds % 86400) / 3600) + "h";
    }

    /**
     * Reports a failed claim. Returns true when the claim actually worked.
     */
    public static boolean tellClaimResult(final Player p, final RealMines rm, final PrivateMineTemplate template, final ClaimResult result) {
        switch (result) {
            case OK:
                final RMine mine = rm.getPrivateMinesManager().getMineOf(p.getUniqueId(), template.getID());
                TranslatableLine.PRIVATE_MINE_CLAIMED
                        .setV1(ReplacableVar.MINE.eq(mine == null ? template.getID() : mine.getDisplayName())).send(p);
                if (template.getCost() > 0 && !p.hasPermission(PrivateMinesManager.ADMIN_PERMISSION)) {
                    TranslatableLine.PRIVATE_MINE_CLAIM_COST
                            .setV1(ReplacableVar.VALUE.eq(String.valueOf(template.getCost()))).send(p);
                }
                return true;
            case DISABLED:
                TranslatableLine.PRIVATE_MINE_DISABLED.send(p);
                return false;
            case NO_PERMISSION:
                TranslatableLine.SYSTEM_ERROR_PERMISSION.send(p);
                return false;
            case ALREADY_OWNED:
                TranslatableLine.PRIVATE_MINE_ALREADY_OWNED.send(p);
                return false;
            case LIMIT_REACHED:
                TranslatableLine.PRIVATE_MINE_LIMIT_REACHED
                        .setV1(ReplacableVar.COUNT.eq(String.valueOf(rm.getPrivateMinesManager().getMaxMinesPerPlayer()))).send(p);
                return false;
            case NO_ECONOMY:
                TranslatableLine.PRIVATE_MINE_NO_ECONOMY.send(p);
                return false;
            case INSUFFICIENT_FUNDS:
                TranslatableLine.PRIVATE_MINE_INSUFFICIENT_FUNDS
                        .setV1(ReplacableVar.VALUE.eq(String.valueOf(template.getCost()))).send(p);
                return false;
            case WORLD_MISSING:
                TranslatableLine.PRIVATE_MINE_WORLD_MISSING.send(p);
                return false;
            case NO_FREE_SLOT:
                TranslatableLine.PRIVATE_MINE_NO_FREE_SLOT.send(p);
                return false;
            case UNSUPPORTED_TYPE:
                TranslatableLine.PRIVATE_MINE_UNSUPPORTED_TYPE.send(p);
                return false;
            default:
                TranslatableLine.PRIVATE_MINE_CLAIM_FAILED.send(p);
                return false;
        }
    }

    @Default
    @Permission("realmines.privatemines")
    public void defaultCommand(final CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }
        if (!checkEnabled(commandSender)) {
            return;
        }
        new PrivateMinesGUI(this.rm, (Player) commandSender).openInventory((Player) commandSender);
    }

    @SubCommand("claim")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine claim <template>")
    @SuppressWarnings("unused")
    public void claimcmd(final CommandSender commandSender, @Suggestion("#privatetemplates") final String templateID) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }
        if (!checkEnabled(commandSender)) {
            return;
        }

        final Player p = (Player) commandSender;
        final PrivateMineTemplate template = this.rm.getPrivateMinesManager().getTemplate(templateID);
        if (template == null) {
            TranslatableLine.PRIVATE_MINE_TEMPLATE_NOT_FOUND.setV1(ReplacableVar.TEMPLATE.eq(templateID)).send(p);
            return;
        }

        tellClaimResult(p, this.rm, template, this.rm.getPrivateMinesManager().claim(p, template));
    }

    @SubCommand("tp")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine tp [template]")
    @SuppressWarnings("unused")
    public void tpcmd(final CommandSender commandSender, @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        this.rm.getMineManager().teleport(p, mine, false, true);
    }

    @SubCommand("info")
    @Permission("realmines.privatemines")
    @SuppressWarnings("unused")
    public void infocmd(final CommandSender commandSender) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final List<RMine> mines = this.rm.getPrivateMinesManager().getMinesOf(p.getUniqueId());
        if (mines.isEmpty()) {
            TranslatableLine.PRIVATE_MINE_NO_MINE.send(p);
            return;
        }

        TranslatableLine.PRIVATE_MINE_INFO_HEADER.send(p);
        for (final RMine mine : mines) {
            final PrivateMineData data = mine.getPrivateData();
            Text.send(p, TranslatableLine.PRIVATE_MINE_INFO_LINE
                    .setV1(ReplacableVar.MINE.eq(mine.getDisplayName()))
                    .setV2(ReplacableVar.TIME.eq(formatTime(mine.getResetValue(RMine.Reset.TIME))))
                    .get().replace("%template%", data.getTemplate()));

            if (data.getExpiresAt() != PrivateMineData.NEVER) {
                TranslatableLine.PRIVATE_MINE_INFO_EXPIRES
                        .setV1(ReplacableVar.TIME.eq(formatTime(data.getSecondsLeft()))).send(p);
            }
        }
    }

    @SubCommand("extend")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine extend [template]")
    @SuppressWarnings("unused")
    public void extendcmd(final CommandSender commandSender, @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        if (mine.getPrivateData().getLifecycle() != PrivateMineData.Lifecycle.TIME_LIMITED) {
            TranslatableLine.PRIVATE_MINE_NOT_TIME_LIMITED.send(p);
            return;
        }

        final ClaimResult result = this.rm.getPrivateMinesManager().extend(p, mine);
        if (result == ClaimResult.OK) {
            TranslatableLine.PRIVATE_MINE_EXTENDED
                    .setV1(ReplacableVar.TIME.eq(formatTime(mine.getPrivateData().getSecondsLeft()))).send(p);
        } else if (result == ClaimResult.INSUFFICIENT_FUNDS) {
            final PrivateMineTemplate template = this.rm.getPrivateMinesManager().getTemplate(mine.getPrivateData().getTemplate());
            TranslatableLine.PRIVATE_MINE_INSUFFICIENT_FUNDS
                    .setV1(ReplacableVar.VALUE.eq(String.valueOf(template == null ? 0D : template.getRenewCost()))).send(p);
        } else if (result == ClaimResult.NO_ECONOMY) {
            TranslatableLine.PRIVATE_MINE_NO_ECONOMY.send(p);
        } else {
            TranslatableLine.PRIVATE_MINE_CLAIM_FAILED.send(p);
        }
    }

    @SubCommand("trust")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine trust <player> [template]")
    @SuppressWarnings("unused")
    public void trustcmd(final CommandSender commandSender, @Suggestion("#players") final String playerName,
                         @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        final OfflinePlayer target = findPlayer(playerName);
        if (target == null) {
            TranslatableLine.PRIVATE_MINE_PLAYER_NOT_FOUND.setV1(ReplacableVar.PLAYER.eq(playerName)).send(p);
            return;
        }

        final PrivateMineData data = mine.getPrivateData();
        final PrivateMineTemplate template = this.rm.getPrivateMinesManager().getTemplate(data.getTemplate());
        final int limit = template == null ? 5 : template.getTrustedLimit();
        if (data.getTrustedCount() >= limit) {
            TranslatableLine.PRIVATE_MINE_TRUSTED_LIMIT.setV1(ReplacableVar.COUNT.eq(String.valueOf(limit))).send(p);
            return;
        }

        if (!data.addTrusted(target.getUniqueId())) {
            TranslatableLine.PRIVATE_MINE_TRUSTED_ALREADY.setV1(ReplacableVar.PLAYER.eq(playerName)).send(p);
            return;
        }

        mine.savePrivateData();
        TranslatableLine.PRIVATE_MINE_TRUSTED_ADDED.setV1(ReplacableVar.PLAYER.eq(playerName)).send(p);
    }

    @SubCommand("untrust")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine untrust <player> [template]")
    @SuppressWarnings("unused")
    public void untrustcmd(final CommandSender commandSender, @Suggestion("#players") final String playerName,
                           @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        final OfflinePlayer target = findPlayer(playerName);
        if (target == null || !mine.getPrivateData().removeTrusted(target.getUniqueId())) {
            TranslatableLine.PRIVATE_MINE_TRUSTED_NOT_FOUND.setV1(ReplacableVar.PLAYER.eq(playerName)).send(p);
            return;
        }

        mine.savePrivateData();
        TranslatableLine.PRIVATE_MINE_TRUSTED_REMOVED.setV1(ReplacableVar.PLAYER.eq(playerName)).send(p);
    }

    @SubCommand("trusted")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine trusted [template]")
    @SuppressWarnings("unused")
    public void trustedcmd(final CommandSender commandSender, @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        final List<UUID> trusted = mine.getPrivateData().getTrustedList();
        if (trusted.isEmpty()) {
            TranslatableLine.PRIVATE_MINE_TRUSTED_EMPTY.send(p);
            return;
        }

        TranslatableLine.PRIVATE_MINE_TRUSTED_HEADER.setV1(ReplacableVar.MINE.eq(mine.getDisplayName())).send(p);
        for (final UUID uuid : trusted) {
            final String name = Bukkit.getOfflinePlayer(uuid).getName();
            Text.send(p, " &7> &f" + (name == null ? uuid.toString() : name));
        }
    }

    @SubCommand("release")
    @Permission("realmines.privatemines")
    @WrongUsage("&c/pmine release [template]")
    @SuppressWarnings("unused")
    public void releasecmd(final CommandSender commandSender, @Optional @Suggestion("#privateownedtemplates") final String templateID) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        final RMine mine = resolveOwn(p, templateID);
        if (mine == null) {
            return;
        }

        final String display = mine.getDisplayName();
        this.rm.getPrivateMinesManager().release(mine);
        TranslatableLine.PRIVATE_MINE_RELEASED.setV1(ReplacableVar.MINE.eq(display)).send(p);
    }

    // ------------------------------------------------------------------ admin

    /**
     * Template administration: {@code /pmine template <create|update|delete|list> [id] [mine]}.
     * <p>
     * Taken as one joined argument and split here rather than as separate parameters, because
     * triumph-cmd only allows a single optional argument and only in last position - and these actions
     * take different numbers of arguments each.
     */
    @SubCommand("template")
    @Permission("realmines.privatemines.admin")
    @WrongUsage("&c/pmine template <create|update|delete|list> [id] [mine]")
    @SuppressWarnings("unused")
    public void templatecmd(final CommandSender commandSender, @Suggestion("#privatetemplateargs") @Join final String arguments) {
        final String[] args = arguments.trim().split("\\s+");
        final String action = args[0].toLowerCase();
        final String id = args.length > 1 ? args[1] : null;
        final String mineName = args.length > 2 ? args[2] : null;

        switch (action) {
            case "list": {
                if (this.rm.getPrivateMinesManager().getTemplates().isEmpty()) {
                    TranslatableLine.PRIVATE_MINE_NO_TEMPLATES.send(commandSender);
                    return;
                }
                for (final PrivateMineTemplate template : this.rm.getPrivateMinesManager().getTemplates()) {
                    Text.send(commandSender, "&f" + template.getID() + " &7- from &f" + template.getSourceMine()
                            + " &7- cost &f" + template.getCost() + " &7- &f" + template.getLifecycle().name().toLowerCase()
                            + " &7- resets every &f" + formatTime(template.getResetTime()));
                }
                return;
            }
            case "create":
            case "update": {
                if (id == null || mineName == null) {
                    Text.send(commandSender, "&c/pmine template " + action + " <id> <mine>");
                    return;
                }

                final RMine source = this.rm.getMineManager().getMine(mineName);
                if (source == null) {
                    TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
                    return;
                }

                try {
                    final PrivateMineTemplate template = this.rm.getPrivateMinesManager().snapshot(source, id);
                    TranslatableLine.PRIVATE_MINE_TEMPLATE_CREATED
                            .setV1(ReplacableVar.TEMPLATE.eq(template.getID()))
                            .setV2(ReplacableVar.MINE.eq(source.getName())).send(commandSender);

                    for (final String problem : template.validate()) {
                        Text.send(commandSender, " &e! &f" + problem);
                    }

                    //placement decides where every claimed copy is built, so it must never be left at defaults.
                    //The world isn't one of those decisions: RealMines makes and owns it.
                    Text.send(commandSender, "&7Placement: world &f" + template.getPlacement().getWorldName()
                            + " &7(created by RealMines)&7, origin &f" + template.getPlacement().getOriginX() + ";"
                            + template.getPlacement().getOriginY() + ";" + template.getPlacement().getOriginZ());
                    Text.send(commandSender, template.getPlacement().hasPlatform()
                            ? "&7Platform: &f" + template.getPlacement().getPlatformWidth() + " &7blocks of &f"
                            + PrivateMinePlatform.walkwayMaterial().name() + " &7around each copy, fenced with barriers"
                            : "&7Platform: &fnone&7, so copies are built with nothing around them");
                    Text.send(commandSender, "&7Edit &fprivate-mines/templates/" + template.getID()
                            + ".yml &7to set the cost, lifetime and where copies are built, then &f/rm reload&7.");
                } catch (final IllegalArgumentException e) {
                    TranslatableLine.PRIVATE_MINE_TEMPLATE_CREATE_FAILED
                            .setV1(ReplacableVar.VALUE.eq(String.valueOf(e.getMessage()))).send(commandSender);
                }
                return;
            }
            case "delete": {
                if (id == null) {
                    Text.send(commandSender, "&c/pmine template delete <id>");
                    return;
                }
                if (this.rm.getPrivateMinesManager().deleteTemplate(id)) {
                    TranslatableLine.PRIVATE_MINE_TEMPLATE_DELETED.setV1(ReplacableVar.TEMPLATE.eq(id)).send(commandSender);
                } else {
                    TranslatableLine.PRIVATE_MINE_TEMPLATE_NOT_FOUND.setV1(ReplacableVar.TEMPLATE.eq(id)).send(commandSender);
                }
                return;
            }
            default:
                Text.send(commandSender, "&c/pmine template <create|update|delete|list> [id] [mine]");
        }
    }

    @SubCommand("list")
    @Permission("realmines.privatemines.admin")
    @WrongUsage("&c/pmine list [player]")
    @SuppressWarnings("unused")
    public void listcmd(final CommandSender commandSender, @Optional @Suggestion("#players") final String playerName) {
        List<RMine> mines = this.rm.getPrivateMinesManager().getPrivateMines();

        if (playerName != null) {
            final OfflinePlayer target = findPlayer(playerName);
            if (target == null) {
                TranslatableLine.PRIVATE_MINE_PLAYER_NOT_FOUND.setV1(ReplacableVar.PLAYER.eq(playerName)).send(commandSender);
                return;
            }
            mines = this.rm.getPrivateMinesManager().getMinesOf(target.getUniqueId());
        }

        if (mines.isEmpty()) {
            TranslatableLine.PRIVATE_MINE_LIST_EMPTY.send(commandSender);
            return;
        }

        TranslatableLine.PRIVATE_MINE_LIST_HEADER.setV1(ReplacableVar.COUNT.eq(String.valueOf(mines.size()))).send(commandSender);
        for (final RMine mine : mines) {
            final PrivateMineData data = mine.getPrivateData();
            Text.send(commandSender, TranslatableLine.PRIVATE_MINE_LIST_LINE
                    .setV1(ReplacableVar.PLAYER.eq(data.getOwnerName()))
                    .setV2(ReplacableVar.MINE.eq(mine.getDisplayName()))
                    .get().replace("%template%", data.getTemplate()));
        }
    }

    @SubCommand("delete")
    @Permission("realmines.privatemines.admin")
    @WrongUsage("&c/pmine delete <player> <template>")
    @SuppressWarnings("unused")
    public void deletecmd(final CommandSender commandSender, @Suggestion("#players") final String playerName,
                          @Suggestion("#privatetemplates") final String templateID) {
        final OfflinePlayer target = findPlayer(playerName);
        if (target == null) {
            TranslatableLine.PRIVATE_MINE_PLAYER_NOT_FOUND.setV1(ReplacableVar.PLAYER.eq(playerName)).send(commandSender);
            return;
        }

        final RMine mine = this.rm.getPrivateMinesManager().getMineOf(target.getUniqueId(), templateID);
        if (mine == null) {
            TranslatableLine.PRIVATE_MINE_NO_MINE.send(commandSender);
            return;
        }

        final String display = mine.getDisplayName();
        this.rm.getPrivateMinesManager().release(mine);
        TranslatableLine.PRIVATE_MINE_RELEASED.setV1(ReplacableVar.MINE.eq(display)).send(commandSender);
    }

    /**
     * Debug: drops a throwaway private mine from a random template on the next free slot and drops you on
     * it, so the platform, the fence and the grid spacing can be looked at without claiming anything.
     * {@code /pmine addsharik clear} takes every one of them back down again.
     */
    @SubCommand("addsharik")
    @Permission("realmines.privatemines.admin")
    @WrongUsage("&c/pmine addsharik [clear]")
    @SuppressWarnings("unused")
    public void addsharikcmd(final CommandSender commandSender, @Optional final String action) {
        final Player p = requirePlayer(commandSender);
        if (p == null) {
            return;
        }

        if (action != null && action.equalsIgnoreCase("clear")) {
            final int removed = this.rm.getPrivateMinesManager().clearDebugMines();
            Text.send(commandSender, removed == 0
                    ? "&7There are no &f" + PrivateMinesManager.DEBUG_OWNER + " &7mines to remove."
                    : "&aRemoved &f" + removed + " &a" + PrivateMinesManager.DEBUG_OWNER + " mine(s).");
            return;
        }

        //there is nothing to look at anywhere else, and it would put mines in a world nobody expects
        final World world = PrivateMinesWorld.peek();
        if (world == null || !p.getWorld().getName().equals(world.getName())) {
            Text.send(commandSender, "&cRun this from inside the &f" + PrivateMinesWorld.NAME + " &cworld.");
            return;
        }

        final List<PrivateMineTemplate> templates = new ArrayList<>(this.rm.getPrivateMinesManager().getTemplates());
        if (templates.isEmpty()) {
            TranslatableLine.PRIVATE_MINE_NO_TEMPLATES.send(commandSender);
            return;
        }

        final PrivateMineTemplate template = templates.get(RealMinesAPI.getRand().nextInt(templates.size()));
        final RMine mine = this.rm.getPrivateMinesManager().spawnDebugMine(template,
                PrivateMinesManager.DEBUG_OWNER + (RealMinesAPI.getRand().nextInt(999) + 1));
        if (mine == null) {
            Text.send(commandSender, "&cCouldn't place one from template &f" + template.getID()
                    + "&c. The console says why.");
            return;
        }

        final PrivateMineData data = mine.getPrivateData();
        Text.send(commandSender, "&aSpawned &r" + mine.getDisplayName() + " &afrom template &f" + template.getID()
                + " &aon slot &f" + data.getSlot() + "&a.");
        Text.send(commandSender, data.getPlatformWidth() > 0
                ? "&7Platform: &f" + data.getPlatformWidth() + " &7wide, fence &f"
                + (mine.getMineCuboid() == null ? "?" : mine.getMineCuboid().getSizeY()) + " &7high"
                : "&7Platform: &fnone&7.");
        Text.send(commandSender, "&7Remove them all again with &f/pmine addsharik clear&7.");

        this.rm.getMineManager().teleport(p, mine, true, false);
    }

    // ------------------------------------------------------------------ helpers

    private boolean checkEnabled(final CommandSender sender) {
        if (!this.rm.getPrivateMinesManager().isEnabled()) {
            TranslatableLine.PRIVATE_MINE_DISABLED.send(sender);
            return false;
        }
        return true;
    }

    private Player requirePlayer(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(sender);
            return null;
        }
        return checkEnabled(sender) ? (Player) sender : null;
    }

    /**
     * The caller's own mine for that template, or their only one when no template was given.
     */
    private RMine resolveOwn(final Player p, final String templateID) {
        if (templateID != null) {
            final RMine mine = this.rm.getPrivateMinesManager().getMineOf(p.getUniqueId(), templateID);
            if (mine == null) {
                TranslatableLine.PRIVATE_MINE_NO_MINE.send(p);
            }
            return mine;
        }

        final List<RMine> mines = this.rm.getPrivateMinesManager().getMinesOf(p.getUniqueId());
        if (mines.isEmpty()) {
            TranslatableLine.PRIVATE_MINE_NO_MINE.send(p);
            return null;
        }
        if (mines.size() > 1) {
            //ambiguous: say which ones they have so they can pick
            TranslatableLine.PRIVATE_MINE_INFO_HEADER.send(p);
            mines.forEach(m -> Text.send(p, " &7> &f" + m.getPrivateData().getTemplate()));
            return null;
        }
        return mines.get(0);
    }

    /**
     * Finds a player by name without ever going to Mojang: online players first, then the server's own
     * cached offline players. {@code Bukkit.getOfflinePlayer(String)} would block the main thread on a
     * web request for an unknown name.
     */
    public static OfflinePlayer findPlayer(final String name) {
        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(offline.getName())) {
                return offline;
            }
        }
        return null;
    }
}
