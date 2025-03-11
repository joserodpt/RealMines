package joserodpt.realmines.api.mine.components.actions;

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

import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionCommand extends MineAction {

    private final CommandSender cmdSndr = Bukkit.getServer().getConsoleSender();
    private String command;

    //for existing
    public MineActionCommand(final String id, final String mineID, final Double chance, final String command) {
        super(id, mineID, chance);
        this.command = command;
    }

    //generate new
    public MineActionCommand(final String mineID, final Double chance, final String command) {
        super(mineID, chance);
        this.command = command;
    }

    public void execute(final Player p, final Location l) {
        if (super.getMine() == null) {
            return;
        }

        String cmd2Exec = this.command.replace("%player%", p.getName()).replace("%blockloc%", Text.location2Command(l));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            cmd2Exec = PlaceholderAPI.setPlaceholders(p, cmd2Exec);
        }
        Bukkit.getServer().dispatchCommand(cmdSndr, cmd2Exec);
    }

    @Override
    public MineActionType getType() {
        return MineActionType.EXECUTE_COMMAND;
    }

    @Override
    public String getValueString() {
        return this.command;
    }

    @Override
    public String getValue() {
        return this.command;
    }

    @Override
    public ItemStack getIcon() {
        return Items.createItem(Material.COMMAND_BLOCK, 1, getType().getDisplayName() + " &r&f- " + Text.formatPercentages(super.getChance() / 100) + "%", Arrays.asList("&fCommand: &b/" + this.command, "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the command.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setCommand(String s) {
        this.command = s;
    }

    @Override
    public String toString() {
        return "MineActionCommand{" +
                "command='" + command + '\'' +
                '}';
    }
}
