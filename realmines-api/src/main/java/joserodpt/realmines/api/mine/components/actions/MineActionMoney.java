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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionMoney extends MineAction {

    private Double money;

    public MineActionMoney(final String id, final String mineID, final Double chance, final Double money) {
        super(id, mineID, chance);
        this.money = money;
    }

    public void execute(final Player p, final Location l, double randomChance) {
        if (randomChance < super.getChance()) {
            if (RealMinesAPI.getInstance().getEconomy() != null) {
                RealMinesAPI.getInstance().getEconomy().depositPlayer(p, money);
                if (RMMinesConfig.file().getBoolean(super.getMineID() + ".Settings.Discard-Break-Action-Messages"))
                    TranslatableLine.MINE_BREAK_ACTION_GIVE_MONEY.setV1(TranslatableLine.ReplacableVar.MONEY.eq(Text.formatNumber(money))).send(p);
            } else {
                Bukkit.getLogger().warning("Economy not found or Vault not installed. Please install a compatible economy plugin.");
            }
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
        return Items.createItem(Material.EMERALD, 1, "&b&lGive Money &r&f- " + super.getChance() + "%", Arrays.asList("&fAmount: &b" + Text.formatNumber(this.money), "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the amount.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
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
