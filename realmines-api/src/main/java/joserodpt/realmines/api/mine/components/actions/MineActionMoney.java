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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.RMineSettings;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionMoney extends MineAction {

    private Double money;

    //for existing
    public MineActionMoney(final String id, final String mineID, final Double chance, final Double money) {
        super(id, mineID, chance);
        this.money = money;
    }

    //generate new
    public MineActionMoney(final String mineID, final Double chance, final Double money) {
        super(mineID, chance);
        this.money = money;
    }

    public void execute(final Player p, final Location l, double randomChance) {
        if (super.getMine() == null) {
            return;
        }
        if (randomChance < super.getChance()) {
            if (RealMinesAPI.getInstance().getEconomy() != null) {
                RealMinesAPI.getInstance().getEconomy().depositPlayer(p, money);
                if (!super.getMine().getSettingBool(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES))
                    TranslatableLine.MINE_BREAK_ACTION_DROP_ITEM.send(p);
            } else {
                RealMinesAPI.getInstance().getLogger().warning("Economy not found or Vault not installed. Please install a compatible economy plugin. Skipping break action ID " + getID());
            }
        }
    }

    @Override
    public MineActionType getType() {
        return MineActionType.GIVE_MONEY;
    }

    @Override
    public String getValueString() {
        return Text.formatNumber(this.money);
    }

    @Override
    public Double getValue() {
        return this.money;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(Material.EMERALD, 1, getType().getDisplayName() + " &r&f- " + Text.formatPercentages(super.getChance()) + "%", Arrays.asList("&fAmount: &b" + Text.formatNumber(this.money), "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the amount.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
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
