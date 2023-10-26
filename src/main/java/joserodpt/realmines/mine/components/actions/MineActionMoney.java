package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionMoney extends MineAction {

    private Double money;

    public MineActionMoney(final String id, final Double chance, final Double money) {
        super(id, chance);
        this.money = money;
    }

    public void execute(final Player p, final Location l, double randomChance) {
        if (randomChance < super.getChance()) {
            RealMines.getPlugin().getEconomy().depositPlayer(p, money);
            Text.send(p, Language.file().getString("Mines.Break-Actions.Give-Money").replace("%money%", Text.formatNumber(money)));
        }
    }

    @Override
    public MineAction.Type getType() {
        return MineAction.Type.GIVE_MONEY;
    }

    @Override
    public Double getValue() {
        return this.money;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(Material.EMERALD, 1, "&b&lGive Money &r&f- " + super.getChance() + "%", Arrays.asList("&fAmount: &b" + Text.formatNumber(this.money), "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the amount.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setAmount(Double d) {
        this.money = d;
    }

    @Override
    public String toString() {
        return "MineActionMoney{" +
                "money=" + money +
                ", chance=" + super.getChance() +
                '}';
    }
}
