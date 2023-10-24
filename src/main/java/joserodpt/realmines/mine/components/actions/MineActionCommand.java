package joserodpt.realmines.mine.components.actions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MineActionCommand extends MineAction {

    private String command;
    public MineActionCommand(final String id, final Double chance, final String command) {
        super(id, chance);
        this.command = command;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), this.command.replace("%player%", p.getName()));
        }
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM;
    }

    @Override
    public String getValue() {
        return this.command;
    }

    @Override
    public String toString() {
        return "MineActionCommand{" +
                "command='" + command + '\'' +
                '}';
    }
}
