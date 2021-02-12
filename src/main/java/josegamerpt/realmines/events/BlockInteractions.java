package josegamerpt.realmines.events;

import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.managers.MineManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class BlockInteractions implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).contains("[realmines]") || event.getLine(0).contains("[RealMines]")) {
            event.setLine(0, "§7[§9Real§bMines§7]");
            String name = event.getLine(1);
            if (MineManager.exists(name)) {
                String modif = event.getLine(2);
                if (MineManager.signset.contains(modif)) {
                    Mine m = MineManager.getMine(name);
                    m.addSign(event.getBlock(), modif);
                    m.updateSigns();
                } else {
                    event.setLine(1, "§4Setting not");
                    event.setLine(2, "§4found");
                }
            } else {
                event.setLine(1, "§4Mine not");
                event.setLine(2, "§4found");
            }
        }
    }
}