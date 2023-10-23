package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    public String toString() {
        return "MineActionMoney{" +
                "money=" + money +
                ", chance=" + super.getChance() +
                '}';
    }
}
