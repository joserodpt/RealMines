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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
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

    public MineActionCommand(final String id, final String mineID, final Double chance, final String command) {
        super(id, mineID, chance);
        this.command = command;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (super.getMine() == null) {
            return;
        }

        if (randomChance < super.getChance()) {
            String cmd2Exec = this.command.replace("%player%", p.getName()).replace("%blockloc%", Text.location2Command(l));
            if (!cmd2Exec.startsWith("/")) {
                cmd2Exec = "/" + cmd2Exec;
            }
            Bukkit.getServer().dispatchCommand(cmdSndr, cmd2Exec);
        }
    }

    @Override
    public Type getType() {
        return Type.EXECUTE_COMMAND;
    }

    @Override
    public String getValue() {
        return this.command;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(Material.COMMAND_BLOCK, 1, "&b&lExecute Command &r&f- " + super.getChance() + "%", Arrays.asList("&fCommand: &b/" + this.command, "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the command.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
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
